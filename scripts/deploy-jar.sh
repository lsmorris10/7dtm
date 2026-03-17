#!/bin/bash
set -e

PUBLIC_DIR="public"
HTML_FILE="$PUBLIC_DIR/index.html"
INITIAL_VERSION="0.1.0-alpha"

if [ -n "$1" ]; then
  jar_file="$1"
else
  jar_file=$(ls -t build/libs/BrutalZombieHordeSurvival-*.jar 2>/dev/null | head -1)
fi

if [ -z "$jar_file" ] || [ ! -f "$jar_file" ]; then
  echo "ERROR: No jar file found. Usage: $0 [path/to/jar]"
  exit 1
fi

jar_name=$(basename "$jar_file")
new_version=$(echo "$jar_name" | sed 's/BrutalZombieHordeSurvival-\(.*\)\.jar/\1/')

echo "Found: $jar_name (version: $new_version)"

if [ "$new_version" = "$INITIAL_VERSION" ]; then
  echo "Version is $INITIAL_VERSION — replacing old jar(s)..."
  rm -f "$PUBLIC_DIR"/BrutalZombieHordeSurvival-*.jar
  cp "$jar_file" "$PUBLIC_DIR/"

  sed -i "s|href=\"\./BrutalZombieHordeSurvival-[^\"]*\.jar\"|href=\"./$jar_name\"|g" "$HTML_FILE"
  sed -i "s|⬇ Download v[^<]*|⬇ Download v$new_version|g" "$HTML_FILE"

  python3 -c "
import re
with open('$HTML_FILE', 'r') as f:
    html = f.read()
html = re.sub(r'<!-- VERSION_MENU_START -->.*?<!-- VERSION_MENU_END -->\n?', '', html, flags=re.DOTALL)
with open('$HTML_FILE', 'w') as f:
    f.write(html)
"
else
  echo "New version detected — keeping all versions..."
  cp "$jar_file" "$PUBLIC_DIR/"

  sed -i "s|href=\"\./BrutalZombieHordeSurvival-[^\"]*\.jar\"|href=\"./$jar_name\"|g" "$HTML_FILE"
  sed -i "s|⬇ Download v[^<]*|⬇ Download v$new_version|g" "$HTML_FILE"

  python3 << PYEOF
import os, re, glob

public_dir = "public"
html_file = os.path.join(public_dir, "index.html")
latest_name = "$jar_name"

jars = sorted(glob.glob(os.path.join(public_dir, "BrutalZombieHordeSurvival-*.jar")), reverse=True)
other_jars = [os.path.basename(j) for j in jars if os.path.basename(j) != latest_name]

with open(html_file, "r") as f:
    html = f.read()

html = re.sub(r'<!-- VERSION_MENU_START -->.*?<!-- VERSION_MENU_END -->\n?', '', html, flags=re.DOTALL)

if other_jars:
    items = ""
    for j in other_jars:
        ver = j.replace("BrutalZombieHordeSurvival-", "").replace(".jar", "")
        items += f'\n        <li><a href="./{j}" download>v{ver}</a></li>'

    menu = f"""<!-- VERSION_MENU_START -->
    <div id="version-menu" class="version-menu">
      <button class="version-toggle" onclick="document.getElementById('version-list').classList.toggle('open')">Other Versions ▾</button>
      <ul id="version-list" class="version-list">{items}
      </ul>
    </div>
    <!-- VERSION_MENU_END -->
    """

    html = html.replace('<p class="download-note">', menu + '<p class="download-note">')
    print(f"Version menu updated with {len(other_jars)} older version(s).")

with open(html_file, "w") as f:
    f.write(html)
PYEOF

fi

echo ""
echo "=== Deployed versions ==="
ls -1 "$PUBLIC_DIR"/BrutalZombieHordeSurvival-*.jar 2>/dev/null | while read f; do
  echo "  $(basename "$f")"
done
echo "Done."
