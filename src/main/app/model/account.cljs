(ns app.model.account
  (:require
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defmutation save-account [{:account/keys [id] :as account}]
  (ok-action [{:keys [state]}]
    ;; FIXME: Bug. The use-* helpers don't get a refresh notification on this modification
    (swap! state fs/entity->pristine* [:account/id id]))
  (remote [_] true))
