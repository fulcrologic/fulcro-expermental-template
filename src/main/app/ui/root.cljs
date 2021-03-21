(ns app.ui.root
  (:require
    [app.model.account :refer [save-account]]
    [app.application :refer [SPA]]
    [com.fulcrologic.fulcro.alpha.raw-components2 :as raw2 :refer [create-element]]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b]]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
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
  {:query         [:account/id :account/name :account/password]
   :initial-state {:account/id :param/id}
   :ident         :account/id}
  (div :.ui.basic.segment
    (dom/h3 :.ui.header "Account")
    (div :.ui.form
      (field {:label    "Name"
              :valid?   true
              :value    (or name "")
              :onChange (fn [evt]
                          (m/set-string!! this :account/name :event evt))})
      (field {:label    "Password"
              :valid?   true
              :value    (or password "")
              :onChange (fn [evt]
                          (m/set-string!! this :account/password :event evt))})
      (dom/button :.ui.primary.button
        {:onClick (fn [] (comp/transact! this [(save-account props)]))}
        "Save"))))

(def ui-account (raw2/factory AccountForm {:keyfn :account/id}))

(defn Settings [_]
  (raw2/with-fulcro SPA
    (let [account (raw2/use-component SPA AccountForm {:ident                [:account/id 1]
                                                       :initial-state-params {:id 1}})]
      (hooks/use-effect
        (fn [] (df/load! SPA :current-account AccountForm))
        [])
      (div :.ui.container.segment
        (h3 "Settings")
        (ui-account account)))))

(def ui-settings #(raw2/create-element Settings))

(defmutation change-route [{:keys [route]}]
  (action [{:keys [state]}]
    (when (#{:main :settings} route)
      (swap! state assoc-in [:component/id ::ui-router :current-route] route))))

(defsc Router [this {:keys [current-route]}]
  {:query         [:current-route]
   :ident         (fn [] [:component/id ::ui-router])
   :initial-state {:current-route :main}}
  (case current-route
    :main (ui-main)
    :settings (ui-settings)))

(def ui-router (comp/factory Router {}))

(defsc Root [this {:root/keys [router]}]
  {:query         [{:root/router (comp/get-query Router)}]
   :initial-state {:root/router {}}}
  (let [current-tab (some-> router :current-route)]
    (div :.ui.container
      (div :.ui.secondary.pointing.menu
        (dom/a :.item {:classes [(when (= :main current-tab) "active")]
                       :onClick (fn [] (comp/transact! this [(change-route {:route :main})]))} "Main")
        (dom/a :.item {:classes [(when (= :settings current-tab) "active")]
                       :onClick (fn [] (comp/transact! this [(change-route {:route :settings})]))} "Settings"))
      (div :.ui.grid
        (div :.ui.row
          (ui-router router))))))
