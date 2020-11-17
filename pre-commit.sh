#!/bin/sh

echo "========= clean up =========\n\n"
rm -r target/doc/*
rm -r docs/api/*

echo "=========  codox   =========\n\n"
lein codox
cp -r target/doc/* docs/api

