#!/bin/bash

# Readability4K CLI wrapper script
# This makes it easier to run the CLI tool

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd "$SCRIPT_DIR" || exit 1

if [ $# -eq 0 ]; then
  echo "Usage: $0 <url> [format] [charThreshold]"
  echo ""
  echo "Examples:"
  echo "  $0 https://example.com/article"
  echo "  $0 https://example.com/article text"
  echo "  $0 https://example.com/article metadata 300"
  echo ""
  echo "Run with -h or --help for full documentation"
  exit 1
fi

if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
  ./gradlew :cli:run --quiet --args="" 2>/dev/null
  exit 0
fi

./gradlew :cli:run --quiet --console=plain --args="$*" 2>&1 | grep -v "Ignoring init script"
