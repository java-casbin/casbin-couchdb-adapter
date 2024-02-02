# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -xe

VERSION=$(./gradlew -q printVersion)
BUNDLE_DIR="build/bundle/io/github/java-casbin/casbin-couchdb-adapter/$VERSION"

rm -rf build/bundle
rm -rf build/libs

./gradlew bundle -x test

mkdir -p $BUNDLE_DIR

cp -r build/libs/** $BUNDLE_DIR

for file in $BUNDLE_DIR/*; do
  gpg -b --armor $file
done

pushd build/bundle
rm -f ../bundle.zip
zip -r ../bundle.zip *
popd
