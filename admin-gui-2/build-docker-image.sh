#!/bin/bash
#
# Copyright 2020-2025 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

SOURCE_DIR="./dist/admin-gui/browser/"
DEST_DIR="../spvitamin-spring-admin/src/main/resources/public/admin-gui/"

docker run -it --rm -v $(pwd):/opt/app np/node-angular-18-2 /opt/app/build.sh
rm -R "$DEST_DIR"
mkdir -p "$DEST_DIR"
cp -r "$SOURCE_DIR"* "$DEST_DIR"
echo "Compiled frontend successfully copied: $SOURCE_DIR -> $DEST_DIR"
