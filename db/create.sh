PGPASSWORD=postgres psql postgres -U postgres psql 2>/dev/null << EOF
create database artifact_storage;
EOF
