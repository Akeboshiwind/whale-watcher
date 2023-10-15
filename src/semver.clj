(ns semver)

(defn semver? [tag]
  (boolean (re-matches #"^v?\d+(\.\d+(\.\d+)?)?$" tag)))

(defn semver [tag]
  (when-let [match (re-find #"^v?(\d+)(\.(\d+)(\.(\d+))?)?$"
                             tag)]
    (let [[_ major _ minor _ patch] match]
      {:major (Integer/parseInt major)
       :minor (if minor (Integer/parseInt minor) 0)
       :patch (if patch (Integer/parseInt patch) 0)})))

(comment
  (for [tag ["1" "1.2" "1.2.3"
             "v1" "v1.2" "v1.2.3"
             "12" "12.23" "12.23.34"
             "test" "1.2.3-SNAPSHOT" "1.2.3.4"]]
    (do
      (println tag "?" (semver? tag))
      (println tag "->" (semver tag)))))

(comment
  (compare 2 1) ; => 1
  (compare 1 2) ; => -1
  (compare 1 1) ; => 0
  ,)

(defn compare-semver [a b]
  (let [major (compare (:major a) (:major b))]
    (if (not= major 0)
      major
      (let [minor (compare (:minor a) (:minor b))]
        (if (not= minor 0)
          minor
          (compare (:patch a) (:patch b)))))))

(defn >semver [a b]
  (> (compare-semver a b) 0))

(comment
  (do
    (assert (= 1 (compare-semver (semver "1.2.3") (semver "1.2.2"))))
    (assert (= -1 (compare-semver (semver "1.2.3") (semver "1.2.4"))))
    (assert (= 0 (compare-semver (semver "1.2.3") (semver "1.2.3"))))

    (assert (= 1 (compare-semver (semver "12.23.34") (semver "12.23.33"))))
    (assert (= -1 (compare-semver (semver "1") (semver "1.2"))))
    (assert (= 0 (compare-semver (semver "1") (semver "1.0.0"))))

    (assert (false? (>semver (semver "1") (semver "1.0.0"))))
    (assert (true? (>semver (semver "2") (semver "1"))))
    (assert (false? (>semver (semver "1.2.3") (semver "1.2.4"))))))
