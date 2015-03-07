(ns pe-apptxn-restsupport.resource-test
  (:require [clojure.test :refer :all]
            [clojure.data.json :as json]
            [datomic.api :refer [q db] :as d]
            [compojure.core :refer [defroutes ANY]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [compojure.handler :as handler]
            [ring.mock.request :as mock]
            [pe-apptxn-restsupport.meta :as meta]
            [pe-apptxn-restsupport.resource-support :as apptxnres]
            [pe-apptxn-restsupport.version.resource-support-v001]
            [pe-apptxn-core.core :as apptxncore]
            [pe-datomic-testutils.core :as dtucore]
            [pe-rest-testutils.core :as rtucore]
            [pe-core-utils.core :as ucore]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [pe-apptxn-restsupport.test-utils :refer [apptxn-schema-files
                                                      db-uri
                                                      apptxn-partition
                                                      apptxnmt-subtype-prefix
                                                      apptxnhdr-auth-token
                                                      apptxnhdr-error-mask
                                                      apptxnhdr-apptxn-id
                                                      apptxnhdr-useragent-device-make
                                                      apptxnhdr-useragent-device-os
                                                      apptxnhdr-useragent-device-os-version
                                                      base-url
                                                      entity-uri-prefix
                                                      entity-uri-template]]))
(def conn (atom nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Routes
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defroutes routes
  (ANY entity-uri-template
       []
       (apptxnres/apptxnset-res @conn
                                apptxn-partition
                                apptxnmt-subtype-prefix
                                apptxnhdr-auth-token
                                apptxnhdr-error-mask
                                base-url
                                entity-uri-prefix
                                apptxnhdr-apptxn-id
                                apptxnhdr-useragent-device-make
                                apptxnhdr-useragent-device-os
                                apptxnhdr-useragent-device-os-version
                                (fn [ctx] true))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Middleware-decorated app
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def app
  (-> routes
      (handler/api)
      (wrap-cookies)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fixtures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(use-fixtures :each (dtucore/make-db-refresher-fixture-fn db-uri
                                                          conn
                                                          apptxn-partition
                                                          apptxn-schema-files))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest apptxn-set-creation
  (testing "API version: 0.0.1, Lang: en-US, Format: JSON"
    (testing "Creation of app-transaction set."
      (is (= 0 (count (apptxncore/all-apptxns @conn))))
      ;; Create App-Transaction Set
      (let [apptxnset {"apptxns" [{"apptxn/id"  "TXN1-3BB05024-B7F6-4B66-838B-F6982FCB08DF",
                                   "apptxn/usecase"  1,
                                   "apptxn/user-agent-device-os-version" "8.1",
                                   "apptxn/logs" [{"apptxnlog/usecase-event" 1,
                                                   "apptxnlog/timestamp" "Thu, 15 Jan 2015 13:11:42 EST",
                                                   "apptxnlog/in-ctx-err-code" 42,
                                                   "apptxnlog/in-ctx-err-desc" "Baaad!"},
                                                  {"apptxnlog/usecase-event" 2,
                                                   "apptxnlog/timestamp" "Fri, 16 Jan 2015 13:11:42 EST",
                                                   "apptxnlog/in-ctx-err-code" nil}],
                                   "apptxn/user-agent-device-os" "iPhone OS",
                                   "apptxn/user-agent-device-make" "x86_64"},
                                  {"apptxn/id" "TXN6-C1F56AD9-0685-4D55-88A9-509B94B9FB1A",
                                   "apptxn/usecase" 6,
                                   "apptxn/user-agent-device-os-version" "8.0.4",
                                   "apptxn/logs" [{"apptxnlog/usecase-event" 0,
                                                   "apptxnlog/timestamp" "Wed, 14 Jan 2015 13:11:42 EST",
                                                   "apptxnlog/in-ctx-err-desc" nil},
                                                  {"apptxnlog/usecase-event" 1,
                                                   "apptxnlog/timestamp" "Tue, 13 Jan 2015 13:11:42 EST",
                                                   "apptxnlog/in-ctx-err-code" 99,
                                                   "apptxnlog/in-ctx-err-desc" "Mmmkay"},
                                                  {"apptxnlog/usecase-event" 3,
                                                   "apptxnlog/timestamp" "Mon, 12 Jan 2015 13:11:42 EST"}],
                                   "apptxn/user-agent-device-os" "iPhone OS",
                                   "apptxn/user-agent-device-make" "x86_64"}]}
            req (-> (rtucore/req-w-std-hdrs rumeta/mt-type
                                            (meta/mt-subtype-apptxnset apptxnmt-subtype-prefix)
                                            meta/v001
                                            "UTF-8;q=1,ISO-8859-1;q=0"
                                            "json"
                                            "en-US"
                                            :post
                                            entity-uri-template
                                            apptxnhdr-apptxn-id
                                            apptxnhdr-useragent-device-make
                                            apptxnhdr-useragent-device-os
                                            apptxnhdr-useragent-device-os-version)
                    (mock/body (json/write-str apptxnset))
                    (mock/content-type (rucore/content-type rumeta/mt-type
                                                            (meta/mt-subtype-apptxnset apptxnmt-subtype-prefix)
                                                            meta/v001
                                                            "json"
                                                            "UTF-8")))
            resp (app req)]
        (testing "status code" (is (= 202 (:status resp))))
        (testing "App transaction logs actually got written to database."
          (let [apptxn-id "TXN1-3BB05024-B7F6-4B66-838B-F6982FCB08DF"
                apptxn-entid (apptxncore/find-apptxn-by-id @conn apptxn-id)]
            (is (not (nil? apptxn-entid)))
            (let [apptxn-ent (d/entity (d/db @conn) apptxn-entid)]
              (is (not (nil? apptxn-ent)))
              (is (= (:apptxn/usecase apptxn-ent) 1))
              (is (= (:apptxn/user-agent-device-make apptxn-ent) "x86_64"))
              (is (= (:apptxn/user-agent-device-os apptxn-ent) "iPhone OS"))
              (is (= (:apptxn/user-agent-device-os-version apptxn-ent) "8.1"))
              (let [apptxnlogs (apptxncore/apptxnlogs-for-apptxnid @conn apptxn-id)
                    apptxnlogs (sort-by :apptxnlog/timestamp apptxnlogs)]
                (is (= 2 (count apptxnlogs)))
                (let [apptxnlog-entid (ffirst apptxnlogs)
                      apptxnlog-ent (d/entity (d/db @conn) apptxnlog-entid)]
                  (is (= 1 (:apptxnlog/usecase-event apptxnlog-ent)))
                  (is (= 42 (:apptxnlog/in-ctx-err-code apptxnlog-ent)))
                  (is (= "Baaad!" (:apptxnlog/in-ctx-err-desc apptxnlog-ent)))
                  (is (= (ucore/rfc7231str->instant "Thu, 15 Jan 2015 13:11:42 EST")
                         (:apptxnlog/timestamp apptxnlog-ent))))
                (let [apptxnlog-entid (first (second apptxnlogs))
                      apptxnlog-ent (d/entity (d/db @conn) apptxnlog-entid)]
                  (is (= 2 (:apptxnlog/usecase-event apptxnlog-ent)))
                  (is (nil? (:apptxnlog/in-ctx-err-code apptxnlog-ent)))
                  (is (nil? (:apptxnlog/in-ctx-err-desc apptxnlog-ent)))
                  (is (= (ucore/rfc7231str->instant "Fri, 16 Jan 2015 13:11:42 EST")
                         (:apptxnlog/timestamp apptxnlog-ent)))))))
          (let [apptxn-id "TXN6-C1F56AD9-0685-4D55-88A9-509B94B9FB1A"
                apptxn-entid (apptxncore/find-apptxn-by-id @conn apptxn-id)]
            (is (not (nil? apptxn-entid)))
            (let [apptxn-ent (d/entity (d/db @conn) apptxn-entid)]
              (is (not (nil? apptxn-ent)))
              (is (= (:apptxn/usecase apptxn-ent) 6))
              (is (= (:apptxn/user-agent-device-make apptxn-ent) "x86_64"))
              (is (= (:apptxn/user-agent-device-os apptxn-ent) "iPhone OS"))
              (is (= (:apptxn/user-agent-device-os-version apptxn-ent) "8.0.4"))
              (let [apptxnlogs (apptxncore/apptxnlogs-for-apptxnid @conn apptxn-id)
                    apptxnlogs (sort-by :apptxnlog/timestamp (vec apptxnlogs))]
                (is (= 3 (count apptxnlogs)))
                (let [[[apptxnlog-entid]] apptxnlogs
                      apptxnlog-ent (d/entity (d/db @conn) apptxnlog-entid)]
                  (is (= 3 (:apptxnlog/usecase-event apptxnlog-ent)))
                  (is (nil? (:apptxnlog/in-ctx-err-code apptxnlog-ent)))
                  (is (nil? (:apptxnlog/in-ctx-err-desc apptxnlog-ent)))
                  (is (= (ucore/rfc7231str->instant "Mon, 12 Jan 2015 13:11:42 EST")
                         (:apptxnlog/timestamp apptxnlog-ent))))
                (let [[_ [apptxnlog-entid]] apptxnlogs
                      apptxnlog-ent (d/entity (d/db @conn) apptxnlog-entid)]
                  (is (= 1 (:apptxnlog/usecase-event apptxnlog-ent)))
                  (is (= 99 (:apptxnlog/in-ctx-err-code apptxnlog-ent)))
                  (is (= "Mmmkay" (:apptxnlog/in-ctx-err-desc apptxnlog-ent)))
                  (is (= (ucore/rfc7231str->instant "Tue, 13 Jan 2015 13:11:42 EST")
                         (:apptxnlog/timestamp apptxnlog-ent))))
                (let [[_ _ [apptxnlog-entid]] apptxnlogs
                      apptxnlog-ent (d/entity (d/db @conn) apptxnlog-entid)]
                  (is (= 0 (:apptxnlog/usecase-event apptxnlog-ent)))
                  (is (nil? (:apptxnlog/in-ctx-err-code apptxnlog-ent)))
                  (is (nil? (:apptxnlog/in-ctx-err-desc apptxnlog-ent)))
                  (is (= (ucore/rfc7231str->instant "Wed, 14 Jan 2015 13:11:42 EST")
                         (:apptxnlog/timestamp apptxnlog-ent))))))))))))
