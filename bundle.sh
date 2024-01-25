set -xe

BUNDLE_DIR=build/bundle/io/github/java-casbin/casbin-couchdb-adapter/1.0.0-SNAPSHOT

rm -rf build/bundle
rm -rf build/libs

./gradlew bundle -x test

mkdir -p $BUNDLE_DIR

cp -r build/libs/** $BUNDLE_DIR

for file in $BUNDLE_DIR/*; do
  gpg -b --armor $file
done

pushd build/bundle
zip -r ../bundle.zip *
popd
