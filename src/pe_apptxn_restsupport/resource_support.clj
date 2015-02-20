(ns pe-apptxn-restsupport.resource-support
  (:require [datomic.api :refer [q db] :as d]
            [clj-time.core :as t]
            [pe-apptxn-restsupport.meta :as meta]
            [clojure.tools.logging :as log]
            [pe-rest-utils.macros :refer [defprocessor-post
                                          defproctemplate-post
                                          defprocessor-impl-post]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-apptxn-core.core :as apptxncore]))

(declare apptxnset-process-post!)
(declare record-apptxn-async)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handle-apptxnset-post!
  [ctx conn partition user-entid auth-scheme auth-scheme-param-name]
  (rucore/handle-post! ctx
                       conn
                       partition
                       nil
                       nil
                       nil
                       nil ; parent-entids
                       user-entid
                       nil
                       nil
                       apptxnset-process-post!
                       record-apptxn-async
                       auth-scheme
                       auth-scheme-param-name))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn apptxnlog-txn
  "Creates and returns a vector suitable for inclusion in a Datomic transaction
   for persisting the given app-transaction log and event."
  ([ctx
    conn
    partition
    apptxn-usecase
    apptxnlog-usecase-event]
   (apptxnlog-txn ctx
                  conn
                  partition
                  apptxn-usecase
                  apptxnlog-usecase-event
                  nil
                  nil))
  ([ctx
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

(defn record-apptxn-async
  ([ctx
    conn
    partition
    apptxn-usecase
    apptxnlog-usecase-event]
   (record-apptxn-async ctx
                        conn
                        partition
                        apptxn-usecase
                        apptxnlog-usecase-event
                        nil
                        nil))
  ([ctx
    conn
    partition
    apptxn-usecase
    apptxnlog-usecase-event
    apptxnlog-event-in-ctx-err-code
    apptxnlog-event-in-ctx-err-desc]
   (d/transact
    conn
    (apptxnlog-txn ctx
                   conn
                   partition
                   apptxn-usecase
                   apptxnlog-usecase-event
                   apptxnlog-event-in-ctx-err-code
                   apptxnlog-event-in-ctx-err-desc))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; handler processors and hypermedia generators
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defprocessor-post apptxnset-process-post!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; processor templates (these do the actual 'work' - and are invoked after the
;;                      request has been parsed based on its format indicator)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defproctemplate-post
  apptxnset-process-post!-t
  nil
  nil
  identity
  identity
  nil
  nil
  nil
  apptxncore/save-apptxnset-txnmaps
  nil
  nil
  nil
  nil
  nil
  record-apptxn-async
  apptxnlog-txn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Version 0.0.1 processors
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defprocessor-impl-post
  apptxnset-process-post!
  apptxnset-process-post!-t
  meta/mt-subtype-apptxnset
  meta/v001)
