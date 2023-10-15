(ns telegram
  (:require [babashka.http-client :as http]
            [clojure.string :as str]))

(def api-url' "https://api.telegram.org")

(defn api-url [token]
  (str api-url' "/bot" token))

(defn escape-markdown [text]
  (-> text
      (str/replace #"\." "\\\\.")
      (str/replace #"-" "\\\\-")
      (str/replace #">" "\\\\>")))

(defn send-message [token params]
  (http/get (str (api-url token)
                "/sendMessage")
            {:query-params params
             :throw false}))
