#!/bin/sh

P="resources/public"

# js
curl https://code.jquery.com/jquery-3.6.0.min.js -o $P/js/jquery.js
# css
curl https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css -o $P/css/bulma.css
