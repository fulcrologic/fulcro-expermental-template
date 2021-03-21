(ns app.model.account
  (:require
    [app.model.mock-database :as db]
    [datascript.core :as d]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]))

(>defn get-account [db id subquery]
  [any? uuid? vector? => (? map?)]
  (d/pull db subquery [:account/id id]))

(defresolver account-resolver [{:keys [db] :as env} {:account/keys [id]}]
  {::pc/input  #{:account/id}
   ::pc/output [:account/name :account/password]}
  (get-account db id [:account/name :account/password]))

(defresolver current-account-resolver [{:keys [db] :as env} _]
  {::pc/output [{:current-account [:account/id]}]}
  {:current-account {:account/id 1}})

(defmutation save-account [env account]
  {}
  (d/transact db/conn [account]))

(def resolvers [account-resolver current-account-resolver save-account])
