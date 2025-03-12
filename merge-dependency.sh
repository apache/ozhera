#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


# Set the output file path to be merged
output_file="target/merged_THIRD-PARTY.txt"
sort_unique_file="target/sort-merged_THIRD-PARTY.txt"

# Clear the output file if it already exists
> "$output_file"

# Find the THIRD-PARTY.txt file in the target/classes directory of all submodules and merge them
find . -type f -path "*/target/classes/THIRD-PARTY.txt" -exec cat {} >> "$output_file" \;

echo "merged completed，result store into $output_file"

sort -u $output_file -o $sort_unique_file

echo "sort and uniq into $sort_unique_file"