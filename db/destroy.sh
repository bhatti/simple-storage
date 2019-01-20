PGPASSWORD=postgres psql postgres -U postgres psql 2>/dev/null << EOF
drop database artifact_storage;
EOF
