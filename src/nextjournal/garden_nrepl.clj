(ns nextjournal.garden-nrepl
  (:require [clojure.edn :as edn]
            [nrepl.server :as nrepl]
            [cider.nrepl :as cider]
            [clojure.java.io :as io]))

(defn deps-edn [] (edn/read (java.io.PushbackReader. (io/reader "deps.edn"))))

(defn start-app! [opts]
  (let [{:keys [exec-fn exec-args]} (get-in (deps-edn) [:aliases :nextjournal/garden])]
    ((requiring-resolve exec-fn) (merge exec-args opts))))

(defn start-nrepl! []
  (let [nrepl-server (nrepl/start-server {:bind "0.0.0.0"
                                          :port (some-> (System/getenv "GARDEN_NREPL_PORT") parse-long)
                                          :handler cider/cider-nrepl-handler})
        addr (.. (:server-socket nrepl-server)
                 (getInetAddress)
                 (getHostAddress))
        port (:port nrepl-server)]
    (println (format "Started nREPL server on %s:%s" addr port))))

(defn start! [opts]
  (start-nrepl!)
  (start-app! opts))
