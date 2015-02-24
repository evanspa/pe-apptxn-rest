(ns pe-apptxn-restsupport.test-utils
  (:require [pe-apptxn-restsupport.resource-support :as ressupport]
            [pe-apptxn-restsupport.version.resource-support-v001]
            [pe-apptxn-restsupport.meta :as meta]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [liberator.core :refer [defresource]]))

(def apptxn-schema-filename "apptxn-logging-schema.dtm")

(def db-uri "datomic:mem://apptxns")

(def apptxn-partition
  "The name of the Datomic partition of the application transactions."
  :apptxn)
(def apptxnhdr-auth-token "apptxn-rest-auth-token")
(def apptxnhdr-error-mask "apptxn-rest-error-mask")
(def apptxnhdr-apptxn-id "apptxn-id")
(def apptxnhdr-useragent-device-make "apptxn-rest-useragent-device-make")
(def apptxnhdr-useragent-device-os "apptxn-rest-useragent-device-os")
(def apptxnhdr-useragent-device-os-version "apptxn-rest-useragent-device-os-version")
(def base-url "")
(def entity-uri-prefix "/testing/")
(def entity-uri-template (format "%s%s%s"
                        base-url
                        entity-uri-prefix
                        meta/pathcomp-apptxnset))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Concrete Resource (for testing)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defresource apptxnset-res [conn
                            hdr-auth-token
                            hdr-error-mask
                            base-url
                            entity-uri-prefix
                            hdr-apptxn-id
                            hdr-useragent-device-make
                            hdr-useragent-device-os
                            hdr-useragent-device-os-version]
  :available-media-types (rucore/enumerate-media-types
                          meta/supported-media-types)
  :available-charsets rumeta/supported-char-sets
  :available-languages rumeta/supported-languages
  :allowed-methods [:post]
  :known-content-type? (rucore/known-content-type-predicate meta/supported-media-types)
  :post! (fn [ctx] (ressupport/handle-apptxnset-post! ctx
                                                      conn
                                                      apptxn-partition
                                                      hdr-apptxn-id
                                                      hdr-useragent-device-make
                                                      hdr-useragent-device-os
                                                      hdr-useragent-device-os-version
                                                      base-url
                                                      entity-uri-prefix
                                                      (:uri (:request ctx))
                                                      nil))
  :handle-created (fn [ctx] (rucore/handle-resp ctx
                                                hdr-auth-token
                                                hdr-error-mask)))
