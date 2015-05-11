(ns pe-apptxn-restsupport.version.resource-support-v001
  (:require [datomic.api :refer [q db] :as d]
            [clj-time.core :as t]
            [pe-apptxn-restsupport.meta :as meta]
            [clojure.tools.logging :as log]
            [clojure.walk :refer [keywordize-keys]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-apptxn-core.core :as apptxncore]
            [pe-apptxn-restsupport.resource-support :refer [body-data-in-transform-fn
                                                            body-data-out-transform-fn
                                                            save-new-entity-fn
                                                            apptxn-async-logger
                                                            make-apptxn]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod body-data-in-transform-fn meta/v001
  [version
   conn
   _
   apptxnset
   apptxnlogger]
  (identity apptxnset))

(defmethod body-data-out-transform-fn meta/v001
  [version
   conn
   _
   apptxnset-resp
   apptxnlogger]
  (identity apptxnset-resp))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 save-entity functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod save-new-entity-fn meta/v001
  [version & more]
  (apply apptxncore/save-apptxnset-txnmaps more))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 0.0.1 sync and async transaction log writing functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmethod apptxn-async-logger meta/v001
  ([version
    ctx
    conn
    partition
    hdr-apptxn-id
    hdr-useragent-device-make
    hdr-useragent-device-os
    hdr-useragent-device-os-version
    apptxn-usecase
    apptxnlog-usecase-event]
   (apptxn-async-logger version
                        ctx
                        conn
                        partition
                        hdr-apptxn-id
                        hdr-useragent-device-make
                        hdr-useragent-device-os
                        hdr-useragent-device-os-version
                        apptxn-usecase
                        apptxnlog-usecase-event
                        nil
                        nil))
  ([version
    ctx
    conn
    partition
    hdr-apptxn-id
    hdr-useragent-device-make
    hdr-useragent-device-os
    hdr-useragent-device-os-version
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
                 hdr-apptxn-id
                 hdr-useragent-device-make
                 hdr-useragent-device-os
                 hdr-useragent-device-os-version
                 apptxn-usecase
                 apptxnlog-usecase-event
                 apptxnlog-event-in-ctx-err-code
                 apptxnlog-event-in-ctx-err-desc))))

(defmethod make-apptxn meta/v001
  ([version
    ctx
    conn
    partition
    hdr-apptxn-id
    hdr-useragent-device-make
    hdr-useragent-device-os
    hdr-useragent-device-os-version
    apptxn-usecase
    apptxnlog-usecase-event]
   (make-apptxn version
                ctx
                conn
                partition
                hdr-apptxn-id
                hdr-useragent-device-make
                hdr-useragent-device-os
                hdr-useragent-device-os-version
                apptxn-usecase
                apptxnlog-usecase-event
                nil
                nil))
  ([version
    ctx
    conn
    partition
    hdr-apptxn-id
    hdr-useragent-device-make
    hdr-useragent-device-os
    hdr-useragent-device-os-version
    apptxn-usecase
    apptxnlog-usecase-event
    apptxnlog-event-in-ctx-err-code
    apptxnlog-event-in-ctx-err-desc]
   (let [{{{apptxn-id hdr-apptxn-id} :headers} :request} ctx
         {{{user-agent-device-make hdr-useragent-device-make} :headers} :request} ctx
         {{{user-agent-device-os hdr-useragent-device-os} :headers} :request} ctx
         {{{user-agent-device-os-version hdr-useragent-device-os-version} :headers} :request} ctx
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
