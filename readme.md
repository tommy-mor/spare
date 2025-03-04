# llm-rewrite-inplace

A tool that lets you build your codebase by defining behavior through extensive testing, then letting LLMs infer the implementation. Instead of writing implementation details, you write tests that cover every edge case, preserve only the most critical code snippets, and let the LLM figure out the rest.

## The Vision

Imagine a workflow where:

1. You write comprehensive tests covering every edge case, error condition, and success path
2. You preserve only the most critical code snippets (database calls, security checks, etc.)
3. You run the test suite once, collecting all input/output pairs
4. The LLM analyzes these logs and rewrites the implementation to match all observed behavior

This flips traditional TDD on its head: instead of writing tests to drive code, you write tests to *define* the code, then let the LLM figure out how to make it work.

## Example

Let's say you're building a simple CRUD API for managing users. Instead of implementing all the routes and business logic, you:

1. Write tests covering every scenario:
```python
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
```

2. Preserve only critical snippets:
```clojure
(instrument create-user [req]
  (spare (when-not (valid-email? (get-in req [:body :email]))
           (throw (ex-info "Invalid email" {}))))
  ;; Let the LLM figure out the rest based on test behavior
  (save-to-database req)
  (send-welcome-email req))

(instrument get-user [req]
  (spare (when-not (user-exists? (get-in req [:params :id]))
           (throw (ex-info "User not found" {}))))
  ;; Let the LLM figure out the rest
  (fetch-from-database req))

;; Similar for update-user and delete-user
```

3. Run the test suite once, collecting all input/output pairs:
```clojure
;; Example of collected logs
[{:input {:request-method :post
          :body {:email "test@example.com" :name "Test User"}},
  :output {:status 201
           :body {:id "123" :email "test@example.com" :name "Test User"}}}
 {:input {:request-method :get
          :params {:id "123"}},
  :output {:status 200
           :body {:id "123" :email "test@example.com" :name "Test User"}}}
 {:input {:request-method :put
          :params {:id "123"}
          :body {:name "Updated Name"}},
  :output {:status 200
           :body {:id "123" :email "test@example.com" :name "Updated Name"}}}
 {:input {:request-method :delete
          :params {:id "123"}},
  :output {:status 204}}
 {:input {:request-method :post
          :body {:email "invalid" :name "Test"}},
  :output {:status 400
           :body {:error "Invalid email"}}}]
```

4. The LLM analyzes these logs and rewrites the implementation to match all observed behavior:
```clojure
(instrument create-user [req]
  (spare (when-not (valid-email? (get-in req [:body :email]))
           (throw (ex-info "Invalid email" {}))))
  (let [user (assoc (get-in req [:body])
                    :id (str (random-uuid)))]
    (save-to-database user)
    (send-welcome-email user)
    (-> (response user)
        (status 201))))

(instrument get-user [req]
  (spare (when-not (user-exists? (get-in req [:params :id]))
           (throw (ex-info "User not found" {}))))
  (let [user (fetch-from-database (get-in req [:params :id]))]
    (-> (response user)
        (status 200))))

;; Similar implementations for update-user and delete-user
```

## The Bigger Picture

This would be an interesting way to write software.

- **Comprehensive Testing**: Instead of writing implementation first, you write tests that cover every possible scenario
- **Behavior-Driven Development**: The implementation emerges from the test behavior, not the other way around
- **Continuous Evolution**: As new edge cases are discovered, add more tests and let the LLM update the implementation
- **Documentation as Tests**: Your test suite becomes the single source of truth for how the code should behave

## Future Directions

- **LLM Agent Loop**: Instead of one-shot rewrites, an agent could continuously monitor test results and refine the implementation
- **Multi-Handler Inference**: Scale to entire API surfaces, letting the LLM understand relationships between handlers
- **Production Learning**: Use real production traffic to further refine implementations
- **Safety Nets**: Sandboxed execution, validation, and rollback capabilities for production use
