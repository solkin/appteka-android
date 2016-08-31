#!/usr/bin/env bash

curl \
  -F "status=2" \
  -F "notify=1" \
  -F "notes=Build #$BUDDYBUILD_BUILD_NUMBER" \
  -F "notes_type=0" \
  -F "ipa=@$BUDDYBUILD_WORKSPACE/app/build/outputs/apk/app-release.apk" \
  -F "dsym=@$BUDDYBUILD_WORKSPACE/app/build/outputs/mapping/release/mapping.txt" \
  -H "X-HockeyAppToken: $HOCKEY_API_TOKEN" \
  https://rink.hockeyapp.net/api/2/apps/$HOCKEY_APP_ID/app_versions/upload
