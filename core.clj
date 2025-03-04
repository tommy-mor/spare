(ns llm-rewrite-inplace.core
  (:require [ring.util.response :as resp]
            [rewrite-clj.zip :as z]
            [clojure.java.io :as io]
            [clojure.tools.namespace.repl :refer [refresh]]))

;; Log storage
(defonce logs (atom {}))
(defonce rewritten? (atom {}))

(defmacro spare [& body] `(do ~@body))

;; Mock LLM (replace with real API)
(defn call-llm [prompt]
  (Thread/sleep 500)
  (str "(instrument my-handler [req] (let [id (:id (:params req))] "
       "(spare (ring.util.response/response (str \"Saved \" id)))))"))

(defn strip [source-str]
  (let [zloc (z/of-string source-str)
        stripped (-> zloc
                     (z/find-value z/next 'instrument)
                     (z/down) (z/right) (z/right) ; Skip name, args
                     (z/remove) ; Remove body
                     (z/append-child (filter #(and (seq? %) (= 'spare (first %)))
                                            (z/sexpr (z/up))))
                     (z/root-string))]
    (clojure.string/replace stripped #"nil" "")))

(defn rewrite-inplace [fn-name source-file]
  (let [fn-logs (get @logs fn-name)
        stripped (strip (slurp source-file))
        prompt (str "Given this stripped handler:\n" stripped "\n"
                    "And these logs:\n" (pr-str fn-logs) "\n"
                    "Rewrite the body, preserving `spare` forms.")
        generated (call-llm prompt)]
    (spit source-file generated)
    (refresh)
    (swap! rewritten? assoc fn-name true)))

(defmacro instrument [name args & body]
  `(defn ~name ~args
     (let [result# (do ~@body)]
       (when-not (get @rewritten? '~name)
         (swap! logs update '~name conj {:input ~args :output result#})
         (when (>= (count (get @logs '~name)) 2) ; Wait for some logs
           (rewrite-inplace '~name "src/llm_rewrite_inplace/core.clj")))
       result#)))

;; Example handler
(instrument my-handler [req]
  (let [id (:id (:params req))]
    (spare (resp/response (str "Saved " id)))))
