(ns regctl
  (:require
    [babashka.process :refer [sh]]
    [clojure.string :as str]))

(defn regctl [& args]
  (-> (apply sh "regctl" args)
      :out))

(defn tags [repo]
  (->> (regctl "tag" "ls" repo)
       str/split-lines
       (remove empty?)))

(comment
  (count (tags "ghcr.io/akeboshiwind/rss-filter"))
  (count (tags "debian"))
  (count (tags "does-not-exist")))
