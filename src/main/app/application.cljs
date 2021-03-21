(ns app.application
  (:require
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.alpha.raw-components2 :as raw2]
    ))

(defonce SPA (raw2/fulcro-app
               {:remotes {:remote (net/fulcro-http-remote
                                    {:url "/api"})}}))
