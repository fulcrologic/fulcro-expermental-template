(ns app.model.account
  (:require
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]))

(defmutation save-account [account]
  (remote [_] true))
