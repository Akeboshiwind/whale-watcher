(ns crontab
  (:require [babashka.fs :as fs]
            [babashka.process :refer [sh]]))

;; TODO: Maybe something like this would be useful as a babashka.schedule namespace?

(def base-path "/etc/cron.d")

(defn install! []
  (fs/create-dirs base-path)
  (let [schedule (System/getenv "WATCH_SCHEDULE")]
    (when (empty? schedule)
      (throw (Exception. "WATCH_SCHEDULE must be set")))
    (spit (str base-path "/watcher")
          ;; NOTE: `/proc/1/fd/1` is crond's stdout
          ;; NOTE: We can use `/proc/1` because we're running in a container
          (str schedule " cd /app && /bin/bb --main core/-main > /proc/1/fd/1")))
  (sh "crontab" (str base-path "/watcher")))

(defn run! []
  (sh "crond" "-f"))

(defn install&run! []
  (install!)
  (run!))
