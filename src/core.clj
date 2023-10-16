(ns core
  (:require
    [docker :as d]
    [regctl :as r]
    [semver :refer [semver? semver compare-semver >semver]]
    [telegram :as t]
    [clojure.string :as str]
    [clojure.tools.logging :as log]))

(defn name&tag [full-name]
  (let [[name tag] (str/split full-name #":")]
    {:name name :tag tag}))

(defn latest-tag [repo]
  (->> (r/tags repo)
       (filter semver?)
       (sort-by semver compare-semver)
       last))

(comment
  (let [repo "ubuntu"
        tag "20.04"
        latest-tag (latest-tag repo)]
    (when (>semver (semver latest-tag) (semver tag))
      (println "Later tag found:" latest-tag))))

(defn ignore? [image]
  (-> image :labels (get "whale-watcher.ignore")))

(defn format-update [{:keys [name tag latest-tag]}]
  (str name ":" tag " -> " latest-tag))

(defn format-message [updates]
  (t/escape-markdown
    (str "*Found updates:*\n"
         (str/join "\n"
                   (map format-update updates)))))

(defn sift
  "Returns a vector of [(filter pred coll) (remove pred coll)]"
  [pred coll]
  (let [g (group-by (comp boolean pred) coll)]
    [(g true) (g false)]))

(defn container-updates []
  (let [[images ignored] (->> (d/ps) (sift (complement ignore?)))

        _ (log/info (str "Skipping due to ignore label: "
                         (->> ignored (map :image) (str/join ", "))))

        [images skipped] (->> images
                              (map (comp name&tag :image))
                              (sift :tag))

        _ (log/info (str "Skipping due to missing/filtered tag: "
                         (->> skipped (map :name) (str/join ", "))))

        updates (for [{:keys [name tag]} images]
                  (do
                    (log/info (str "Checking: " name ":" tag))
                    (let [latest-tag (latest-tag name)]
                      (when (>semver (semver latest-tag) (semver tag))
                        {:name name :tag tag :latest-tag latest-tag}))))]
    (->> updates (remove nil?))))

(defn -main [& _args]
  (log/info "Checking for updates")
  (let [token (System/getenv "TELEGRAM_TOKEN")
        chat-id (System/getenv "TELEGRAM_CHAT_ID")]
    (try
      (let [updates (container-updates)]
        (when (seq updates)
          (t/send-message token
                          {:chat_id chat-id
                           :parse_mode "MarkdownV2"
                           :text (format-message updates)})))
      (catch Exception e
        (log/error e "Error checking for updates")
        (t/send-message token
                        {:chat_id chat-id
                         :text "Error checking for updates"}))))
  (log/info "Done!"))
