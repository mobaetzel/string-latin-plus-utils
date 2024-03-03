import json

import requests
import zipfile
import io
import xml.etree.ElementTree as ET
import os
from jinja2 import Environment, FileSystemLoader

DIR_NAME = os.path.dirname(__file__)

template_env = Environment(loader=FileSystemLoader(DIR_NAME + '/templates'))

resource_zip_url: str = 'https://www.xoev.de/sixcms/media.php/13/StringLatin%2012.zip'
resource_zip_path: str = '01-xml-datenbasis/latinchars.xml'
default_namespace: str = 'http://xoev.de/latinchars'


def ns(tag: str) -> str:
    return f'{{{default_namespace}}}{tag}'


resource_zip_response: requests.Response = requests.get(resource_zip_url)
resource_zip_bytes: io.BytesIO = io.BytesIO(resource_zip_response.content)
resource_zip: zipfile.ZipFile = zipfile.ZipFile(resource_zip_bytes, 'r')
xml_content: bytes = resource_zip.read(resource_zip_path)

character_set: ET.Element = ET.fromstring(xml_content)
group_elements: list[ET.Element] = character_set.findall(ns('groups') + '/' + ns('group'))
char_elements: list[ET.Element] = character_set.findall(ns('char'))
sequence_elements: list[ET.Element] = character_set.findall(ns('sequence'))

# ------------------- Generate CharClass Enum ------------------- #

groups: list[(str, str)] = []
for group in group_elements:
    groups.append((
        group.attrib.get('id'),
        group.find(ns('description')).text,
    ))

char_class_enum_template = template_env.get_template('CharClass.java.jinja2')
char_class_enum_rendered = char_class_enum_template.render(groups=groups)
char_class_enum_path = './src/main/java/de/btzl/stringlatinplusutils/generated/CharClass.java'
with open(char_class_enum_path, 'w') as file:
    file.write(char_class_enum_rendered)

# ------------------- Generate Mappings ------------------- #

mappings = {}

for child in char_elements:
    code_point = child.find(ns('cp')).text
    name = child.find(ns('name')).text if child.find(ns('name')) is not None else code_point
    mapping = child.find(ns('mapping'))
    group = child.attrib.get('group')

    mapping_type = mapping.attrib.get('type') if mapping is not None else 'identity'

    if mapping_type == 'mapped':
        mapping_dest = mapping.findall(ns('dest')) if mapping is not None else None
        if mapping_dest is not None:
            code_point_mapping = mappings.get(code_point, {})
            code_point_mapping['_'] = [dest.text for dest in mapping_dest]
            code_point_mapping['$'] = group
            mappings[code_point] = code_point_mapping
        else:
            code_point_mapping = mappings.get(code_point, {})
            code_point_mapping['_'] = [code_point]
            code_point_mapping['$'] = group
            mappings[code_point] = code_point_mapping
            print(f'No mapping destination found for code point: {code_point} / {name}. Setting default mapping {code_point} -> {code_point}')
    elif mapping_type == 'identity':
        code_point_mapping = mappings.get(code_point, {})
        code_point_mapping['_'] = [code_point]
        code_point_mapping['$'] = group
        mappings[code_point] = code_point_mapping
    else:
        raise ValueError(f'Unknown mapping type: {mapping_type} for code point: {code_point} / {name}')

for child in sequence_elements:
    code_points = child.find(ns('cp')).text.split(' ')
    name = child.find(ns('name')).text if child.find(ns('name')) is not None else ' '.join(code_points)
    mapping = child.find(ns('mapping'))
    group = child.attrib.get('group')
    mapping_dest = mapping.findall(ns('dest')) if mapping is not None else None

    if mapping is None:
        print(f'No mapping found for code point: {" ".join(code_points)} / {name}')
        continue

    mapping_type = mapping.attrib.get('type')

    if mapping_type == 'mapped':
        mapping_dest = mapping.findall(ns('dest')) if mapping is not None else None
        if mapping_dest is not None:
            code_point_mapping = mappings
            for code_point in code_points:
                _code_point_mapping = code_point_mapping.get(code_point, {
                    '_': [],
                    '$': group
                })
                code_point_mapping[code_point] = _code_point_mapping
                code_point_mapping = _code_point_mapping
            code_point_mapping['_'] = [dest.text for dest in mapping_dest]
            code_point_mapping['$'] = group
        else:
            raise ValueError(f'No mapping destination found for code point: {" ".join(code_points)} / {name}')
    elif mapping_type == 'identity':
        raise ValueError(f'Identity mapping not supported for sequences for code point: {" ".join(code_points)} / {name}')
    else:
        raise ValueError(f'Unknown mapping type: {mapping_type} form code point: {" ".join(code_points)} / {name}')

nodes = []


def make_node(code_point: str, mapping: dict[str, any]) -> str:
    group = mapping.get('$')
    default = mapping.get('_', [])

    content = f'new CharNode(0x{code_point}, CharClass.{group.upper()}, '
    content += 'new int[]{' + ', '.join(['0x' + x for x in default]) + '}'

    for _code_point, _mapping in mapping.items():
        if _code_point != '_' and _code_point != '$':
            content += ', ' + make_node(_code_point, _mapping)

    return content + ')'


for code_point, mapping in mappings.items():
    nodes.append(make_node(code_point, mapping))

char_nodes_template = template_env.get_template('CharNodes.java.jinja2')
char_nodes_rendered = char_nodes_template.render(nodes=nodes)
char_nodes_path = './src/main/java/de/btzl/stringlatinplusutils/generated/CharNodes.java'
with open(char_nodes_path, 'w') as file:
    file.write(char_nodes_rendered)
