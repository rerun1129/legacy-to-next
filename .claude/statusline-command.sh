#!/bin/bash
input=$(cat)
model=$(echo "$input" | jq -r '.model.display_name // "Unknown"')
used=$(echo "$input" | jq -r '.context_window.used_percentage // 0')
used_int=$(printf '%.0f' "$used")
filled=$(( used_int / 5 ))
empty=$(( 20 - filled ))
bar=""
for i in $(seq 1 $filled); do bar="${bar}█"; done
for i in $(seq 1 $empty); do bar="${bar}░"; done
if [ "$used_int" -lt 40 ]; then
  color="\033[0;32m"
elif [ "$used_int" -lt 60 ]; then
  color="\033[0;33m"
else
  color="\033[0;31m"
fi
session_used=$(echo "$input" | jq -r '.rate_limits.five_hour.used_percentage // 0')
session_used_int=$(printf '%.0f' "$session_used")
if [ "$session_used_int" -lt 40 ]; then
  scolor="\033[0;32m"
elif [ "$session_used_int" -lt 80 ]; then
  scolor="\033[0;33m"
else
  scolor="\033[0;31m"
fi
printf "\033[0;36m%s\033[0m  ${color}[%s]\033[0m ${color}%s%%\033[0m ctx  ${scolor}%s%%\033[0m session" \
  "$model" "$bar" "$used_int" "$session_used_int"
