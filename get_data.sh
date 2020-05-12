#! /bin/bash
mkdir -p src/main/resources/data
wget -c https://datasets.imdbws.com/title.basics.tsv.gz -O - | gunzip >> src/main/resources/data/title.basics.tsv
