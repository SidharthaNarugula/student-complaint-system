#!/bin/bash
# API Testing Script for Student Complaint System
# Replace localhost:8080 with your server address if needed

API_URL="http://localhost:8080"

echo "====== Student Complaint System - API Testing ======"

# 1. Register a new student
echo -e "\n1. Registering a new student..."
curl -X POST "$API_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Johnson",
    "email": "alice@example.com",
    "password": "password123"
  }'

# 2. Login as student
echo -e "\n\n2. Logging in as student..."
curl -X POST "$API_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "email": "alice@example.com",
    "password": "password123"
  }'

# 3. Get current user info
echo -e "\n\n3. Getting current user info..."
curl -X GET "$API_URL/api/auth/user" \
  -H "Content-Type: application/json" \
  -b cookies.txt

# 4. Create a complaint
echo -e "\n\n4. Creating a complaint..."
curl -X POST "$API_URL/api/complaints" \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Broken WiFi in Hostel",
    "description": "WiFi has been down in Block A for 3 days",
    "category": "Infrastructure"
  }'

# 5. Get my complaints
echo -e "\n\n5. Getting my complaints..."
curl -X GET "$API_URL/api/complaints/user" \
  -H "Content-Type: application/json" \
  -b cookies.txt

# 6. Login as admin
echo -e "\n\n6. Logging in as admin..."
# Note: Admin user must exist in database first
curl -X POST "$API_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -c admin_cookies.txt \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }'

# 7. Get all complaints (admin only)
echo -e "\n\n7. Getting all complaints (admin)..."
curl -X GET "$API_URL/api/complaints" \
  -H "Content-Type: application/json" \
  -b admin_cookies.txt

# 8. Update complaint status (admin only)
# Replace {complaint_id} with actual complaint ID from step 5
echo -e "\n\n8. Updating complaint status (admin)..."
curl -X PUT "$API_URL/api/complaints/{complaint_id}" \
  -H "Content-Type: application/json" \
  -b admin_cookies.txt \
  -d '{
    "title": "Broken WiFi in Hostel",
    "description": "WiFi has been down in Block A for 3 days",
    "category": "Infrastructure",
    "status": "IN_REVIEW"
  }'

# 9. Logout
echo -e "\n\n9. Logging out..."
curl -X POST "$API_URL/api/auth/logout" \
  -H "Content-Type: application/json" \
  -b cookies.txt

echo -e "\n\n====== Testing Complete ======"
