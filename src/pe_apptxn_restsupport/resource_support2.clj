(ns pe-apptxn-restsupport.resource-support2
  (:require [datomic.api :refer [q db] :as d]
            [clj-time.core :as t]
            [pe-apptxn-restsupport.meta :as meta]
            [clojure.tools.logging :as log]
            [clojure.walk :refer [keywordize-keys]]
            [pe-rest-utils.core2 :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-apptxn-core.core :as apptxncore]))

(declare body-data-in-transform-fn)
(declare body-data-out-transform-fn)
(declare save-new-entity-fn)
(declare apptxn-async-logger)
(declare make-apptxn)

(defn handle-apptxnset-post!
  [ctx conn partition user-entid auth-scheme auth-scheme-param-name]
  (rucore/put-or-post-invoker ctx
                              :post-as-create-async
                              conn
                              partition
                              nil
                              nil
                              nil
                              [user-entid]
                              nil
                              nil
                              body-data-in-transform-fn
                              body-data-out-transform-fn
                              nil
                              nil
                              nil
                              save-new-entity-fn
                              nil
                              nil
                              nil
                              nil
                              nil
                              nil
                              nil
                              apptxn-async-logger
                              make-apptxn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti body-data-in-transform-fn
  (fn [version body-data]
    version)
  :default meta/v001)

(defmulti body-data-out-transform-fn
  (fn [version body-data]
    version)
  :default meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; save-entity functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti save-new-entity-fn
  (fn [version & more]
    version)
  :default meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; sync and async transaction log writing functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti apptxn-async-logger
  (fn [version & more]
    version)
  :default meta/v001)

(defmulti make-apptxn
  "Creates and returns a vector suitable for inclusion in a Datomic transaction
  for persisting the given app-transaction log and event."
  (fn [version & more]
    version)
  :default meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 sync and async transaction log writing functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod apptxn-async-logger meta/v001
  ([version
    ctx
    conn
    partition
    apptxn-usecase
    apptxnlog-usecase-event]
   (apptxn-async-logger version
                        ctx
                        conn
                        partition
                        apptxn-usecase
                        apptxnlog-usecase-event
                        nil
                        nil))
  ([version
    ctx
    conn
    partition
    apptxn-usecase
    apptxnlog-usecase-event
    apptxnlog-event-in-ctx-err-code
    apptxnlog-event-in-ctx-err-desc]
   (d/transact
    conn
    (make-apptxn version
                 ctx
                 conn
                 partition
                 apptxn-usecase
                 apptxnlog-usecase-event
                 apptxnlog-event-in-ctx-err-code
                 apptxnlog-event-in-ctx-err-desc))))

(defmethod make-apptxn meta/v001
  ([version
    ctx
    conn
    partition
    apptxn-usecase
    apptxnlog-usecase-event]
   (make-apptxn ctx
                conn
                partition
                apptxn-usecase
                apptxnlog-usecase-event
                nil
                nil))
  ([version
    ctx
    conn
    partition
    apptxn-usecase
    apptxnlog-usecase-event
    apptxnlog-event-in-ctx-err-code
    apptxnlog-event-in-ctx-err-desc]
   (let [{{{apptxn-id rumeta/hdr-apptxn-id} :headers} :request} ctx
         {{{user-agent-device-make rumeta/hdr-useragent-device-make} :headers} :request} ctx
         {{{user-agent-device-os rumeta/hdr-useragent-device-os} :headers} :request} ctx
         {{{user-agent-device-os-version rumeta/hdr-useragent-device-os-version} :headers} :request} ctx
         apptxnlog-event-edn-ctx (when (or apptxnlog-event-in-ctx-err-code
                                           apptxnlog-event-in-ctx-err-desc)
                                   (pr-str ctx))]
     (apptxncore/save-apptxnlog-txn conn
                                    partition
                                    apptxn-id
                                    apptxn-usecase
                                    user-agent-device-make
                                    user-agent-device-os
                                    user-agent-device-os-version
                                    apptxnlog-usecase-event
                                    (.toDate (t/now))
                                    apptxnlog-event-in-ctx-err-code
                                    apptxnlog-event-in-ctx-err-desc
                                    apptxnlog-event-edn-ctx))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version body-data]
  (identity body-data))

(defmethod body-data-out-transform-fn meta/v001
  [version body-data]
  (identity body-data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 save-entity functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-new-entity-fn meta/v001
  [version & more]
  (apply apptxncore/save-apptxnset-txnmaps more))
