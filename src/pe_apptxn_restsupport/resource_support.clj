(ns pe-apptxn-restsupport.resource-support
  (:require [datomic.api :refer [q db] :as d]
            [clj-time.core :as t]
            [pe-apptxn-restsupport.meta :as meta]
            [clojure.tools.logging :as log]
            [clojure.walk :refer [keywordize-keys]]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.macros :refer [defmulti-by-version]]
            [pe-rest-utils.meta :as rumeta]
            [pe-apptxn-core.core :as apptxncore]))

(declare body-data-in-transform-fn)
(declare body-data-out-transform-fn)
(declare save-new-entity-fn)
(declare apptxn-async-logger)
(declare make-apptxn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handler
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn handle-apptxnset-post!
  [ctx
   conn
   partition
   hdr-apptxn-id
   hdr-useragent-device-make
   hdr-useragent-device-os
   hdr-useragent-device-os-version
   base-url
   entity-uri-prefix
   entity-uri
   user-entid]
  (rucore/put-or-post-invoker ctx
                              :post-as-create-async
                              conn
                              partition
                              hdr-apptxn-id
                              hdr-useragent-device-make
                              hdr-useragent-device-os
                              hdr-useragent-device-os-version
                              base-url
                              entity-uri-prefix
                              entity-uri
                              nil
                              nil
                              [user-entid]
                              nil
                              nil
                              body-data-in-transform-fn
                              body-data-out-transform-fn
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
                              nil
                              nil
                              apptxn-async-logger
                              make-apptxn))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; body-data transformation functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version body-data-in-transform-fn meta/v001)
(defmulti-by-version body-data-out-transform-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; save-entity functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version save-new-entity-fn meta/v001)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; sync and async transaction log writing functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defmulti-by-version apptxn-async-logger meta/v001)
(defmulti-by-version make-apptxn meta/v001)
