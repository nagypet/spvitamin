#!/bin/bash
docker run -it --rm -p 4200:4200 -v $(pwd):/opt/app np/node-angular-17
