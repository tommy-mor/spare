
user = {
    "email": "test@example.com",
    "name": "Test User"
}
result = req("POST", "/users", data=user)

assert result.status_code == 201
assert result.json()["email"] == "test@example.com"
assert isinstance(result.json()["id"], str)

user_id = "123"
result = req("GET", f"/users/{user_id}")

assert result.status_code == 200
assert result.json()["id"] == user_id

user_id = "123"
updates = {"name": "Updated Name"}
result = req("PUT", f"/users/{user_id}", data=updates)

assert result.status_code == 200
assert result.json()["name"] == "Updated Name"

user_id = "123"
result = req("DELETE", f"/users/{user_id}")

assert result.status_code == 204

invalid_user = {
    "email": "invalid",
    "name": "Test"
}
result = req("POST", "/users", data=invalid_user)

assert result.status_code == 400
assert result.json()["error"] == "Invalid email"

# Test user not found
result = req("GET", "/users/nonexistent")

assert result.status_code == 404
assert result.json()["error"] == "User not found"