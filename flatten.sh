#!/bin/bash

{
  echo '<?xml version="1.0" encoding="UTF-8"?>'
  echo '<files>'

  git ls-files -z | while IFS= read -rd '' file; do
    # Ignora se não for arquivo, for binário ou for um lockfile
    [[ -f "$file" ]] || continue
    file -b --mime-type "$file" | grep -q '^text/' || continue
    [[ "$file" == *package-lock.json || "$file" == *uv.lock ]] && continue

    printf '  <file path="%s">\n    <![CDATA[\n' "$file"
    cat "$file"
    echo '    ]]>
  </file>'
  done

  echo '</files>'
} > flattened-codebase.xml

echo "Arquivo XML gerado: flattened-codebase.xml"
