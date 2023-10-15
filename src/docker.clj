(ns docker
  (:require
    [babashka.process :refer [sh]]
    [clojure.string :as str]))

(defn docker [& args]
  (-> (apply sh "docker" args)
      :out))

(defn parse-ps [line]
  (let [[image labels] (str/split line #"\s+")
        labels (when-not (empty? labels)
                 (->> (str/split labels #",")
                      (map #(str/split % #"="))
                      (into {})))]
    {:image image
     :labels labels}))

(defn ps []
  (->> (docker "ps" "--format" "{{.Image}} {{.Labels}}")
       str/split-lines
       (map parse-ps)))

(comment
  (ps))
