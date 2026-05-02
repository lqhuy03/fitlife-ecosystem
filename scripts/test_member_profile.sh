#!/bin/bash

# Copied test script moved to scripts/
# Make executable if needed

set -e

# Default behavior: call original script in repo root if present
ORIGINAL="$(dirname "$0")/../test_member_profile.sh"
if [ -f "$ORIGINAL" ]; then
  echo "Executing original test script: $ORIGINAL"
  bash "$ORIGINAL"
  exit $?
fi

# If original not found, simple placeholder
echo "No original test script found in repo root. Please run the root script or update this script."
exit 1

