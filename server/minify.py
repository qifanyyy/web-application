import os
from pathlib import Path
import sys

html_minify_cmd = 'html-minifier --collapse-whitespace --remove-comments --remove-optional-tags ' + \
    '--remove-redundant-attributes --remove-script-type-attributes --remove-tag-whitespace --use-short-doctype ' + \
    '{} -o {}'
clean_css_cmd = 'cleancss -O2 {} -o {}'
uglify_js_cmd = 'uglifyjs {} -o {} --compress --mangle'


def _get_output_file_name(input_file):
    if len(sys.argv) < 2 or sys.argv[1] != '--deploy':
        return input_file.parent / (input_file.stem + '.min' + input_file.suffix)
    return input_file


if __name__ == '__main__':
    for f in filter(
            lambda x: x.suffix in {'.html', '.css', '.js'} and '.min' not in set(x.suffixes) and x.is_file(),
            sorted(Path('./WebContent').iterdir()) + sorted(Path('./WebContent/js').iterdir()) + \
                sorted(Path('./WebContent/css').iterdir())
    ):
        print('minifying {}'.format(f.name))
        f = f.absolute()
        if f.suffix == '.html':
            if ret = os.system(html_minify_cmd.format(f, _get_output_file_name(f))) != 0:
                exit(ret)
        elif f.suffix == '.css':
            if ret = os.system(clean_css_cmd.format(f, _get_output_file_name(f))) != 0:
                exit(ret)
        else:  # .js
            if ret = os.system(uglify_js_cmd.format(f, _get_output_file_name(f))) != 0:
                exit(ret)
