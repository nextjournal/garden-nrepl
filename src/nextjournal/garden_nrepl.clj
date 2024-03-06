(ns nextjournal.garden-nrepl
  (:require [clojure.edn :as edn]
            [nrepl.server :as nrepl]
            [cider.nrepl :as cider]
            [clojure.java.io :as io]))

(defn deps-edn [] (edn/read (java.io.PushbackReader. (io/reader "deps.edn"))))

(defn start-app! [opts]
  (let [{:as garden-alias :keys [exec-fn exec-args]} (get-in (deps-edn) [:aliases :nextjournal/garden])]
    (when-not (symbol? exec-fn)
      (throw (ex-info "No :exec-fn under :nextjournal/garden alias in deps.edn" garden-alias)))
    ((requiring-resolve exec-fn) (merge exec-args opts))))

(defn start-nrepl! []
  (let [nrepl-server (nrepl/start-server {:bind "0.0.0.0"
                                          :port (some-> (System/getenv "GARDEN_NREPL_PORT") Integer/parseInt)
                                          :handler cider/cider-nrepl-handler})
        addr (.. (:server-socket nrepl-server)
                 (getInetAddress)
                 (getHostAddress))
        port (:port nrepl-server)]
    (println (format "Started nREPL server on %s:%s" addr port))))

(defn start! [opts]
  (start-nrepl!)
  (start-app! opts))
