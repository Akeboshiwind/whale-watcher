(ns core
  (:require
    [docker :as d]
    [regctl :as r]
    [semver :refer [semver? semver compare-semver >semver]]
    [telegram :as t]
    [clojure.string :as str]))

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

(defn -main [& _args]
  (println "Checking for updates")
  (let [token (System/getenv "TELEGRAM_TOKEN")
        chat-id (System/getenv "TELEGRAM_CHAT_ID")]
    (try
      (let [images (d/ps)

            _ (apply println "Skipping due to ignore label:"
                     (->> images
                          (filter ignore?)
                          (map :image)))

            images (->> images
                        (remove ignore?)
                        (map (comp name&tag :image)))

            _ (apply println "Skipping due to missing/filtered tag:"
                     (->> images
                          (remove :tag)
                          (map :name)))

            images (->> images (filter :tag))

            updates (for [{:keys [name tag]} images]
                      (do
                        (println (str "Checking: " name ":" tag))
                        (let [latest-tag (latest-tag name)]
                          (when (>semver (semver latest-tag) (semver tag))
                            {:name name :tag tag :latest-tag latest-tag}))))
            updates (->> updates (remove nil?))]
        (when (seq updates)
          (t/send-message token
                          {:chat_id chat-id
                           :parse_mode "MarkdownV2"
                           :text (format-message updates)})))
      (catch Exception _
        (t/send-message token
                        {:chat_id chat-id
                         :text "Error checking for updates"}))))
  (println "Done!"))
