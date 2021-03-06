(ns app.ui.root
  (:require
    [app.model.account :refer [save-account]]
    [app.application :refer [SPA]]
    [com.fulcrologic.fulcro.alpha.raw-components2 :as raw2 :refer [create-element]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b]]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.react.hooks :as hooks]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.dom.events :as evt]))

(defn field [{:keys [label valid? error-message] :as props}]
  (let [input-props (-> props (assoc :name label) (dissoc :label :valid? :error-message))]
    (div :.ui.field
      (dom/label {:htmlFor label} label)
      (dom/input input-props)
      (dom/div :.ui.error.message {:classes [(when valid? "hidden")]}
        error-message))))

(defn Main [_]
  (div :.ui.container.segment
    (h3 "Main")
    (p (str "Welcome to the Fulcro template. "
         "The Sign up and login functionalities are partially implemented, "
         "but mostly this is just a blank slate waiting "
         "for your project."))))

(def ui-main #(raw2/create-element Main))

(defsc AccountForm [this {:account/keys [name password] :as props}]
  {:query       [:account/id :account/name :account/password
                 [df/marker-table '_]
                 fs/form-config-join]
   :ident       :account/id
   :form-fields #{:account/name :account/password}}
  (let [loading?        (df/loading? (get-in props [df/marker-table :account]))
        name-valid?     (seq name)
        password-valid? (> (count password) 7)
        form-valid?     (and name-valid? password-valid?)]
    (div :.ui.basic.segment
      (dom/h3 :.ui.header "Account")
      (div :.ui.form {:classes [(when loading? "loading") (when-not form-valid? "error")]}
        (field {:label         "Name"
                :valid?        name-valid?
                :error-message "Name is required"
                :value         (or name "")
                :onChange      (fn [evt]
                                 (m/set-string!! this :account/name :event evt))})
        (field {:label         "Password"
                :valid?        password-valid?
                :error-message "Password must be at least 8 long."
                :value         (or password "")
                :type          "password"
                :onChange      (fn [evt]
                                 (m/set-string!! this :account/password :event evt))})
        (dom/button :.ui.primary.button
          {:onClick (fn [] (comp/transact! this [(save-account props)]))
           :classes [(when-not (fs/dirty? props) "disabled")]}
          "Save")))))

(def ui-account (raw2/factory AccountForm {:keyfn :account/id}))

(defn use-load [k component options]
  (hooks/use-effect
    (fn [] (df/load! SPA k component options))
    []))

(defn Settings [_]
  (raw2/with-fulcro SPA
    (use-load :current-account AccountForm {:target      [:component/id ::form-container :current-account]
                                            :post-action (fn [{:keys [state]}]
                                                           (let [account-ident (get-in @state [:component/id ::form-container :current-account])]
                                                             (swap! state fs/add-form-config* AccountForm account-ident {:destructive? true})))
                                            :marker      :account})
    (let [{:keys [current-account]} (raw2/use-tree SPA (raw2/nc [:component/id {:current-account (comp/get-query AccountForm)}])
                                      {:initial-tree {:component/id ::form-container}})]
      (div :.ui.container.segment
        (h3 "Settings")
        (when current-account
          (ui-account current-account))))))

(def ui-settings #(raw2/create-element Settings))

;; Root has to be a Fulcro component, but it does not have to have anything else.
(defsc Root [this _]
  {}
  (let [current-tab (or (comp/get-state this :route) :main)]
    (div :.ui.container
      (div :.ui.secondary.pointing.menu
        (dom/a :.item {:classes [(when (= :main current-tab) "active")]
                       :onClick (fn [] (comp/set-state! this {:route :main}))} "Main")
        (dom/a :.item {:classes [(when (= :settings current-tab) "active")]
                       :onClick (fn [] (comp/set-state! this {:route :settings}))} "Settings"))
      (div :.ui.grid
        (div :.ui.row
          (case current-tab
            :main (ui-main)
            :settings (ui-settings)))))))
