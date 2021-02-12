"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _regenerator = require("babel-runtime/regenerator");

var _regenerator2 = _interopRequireDefault(_regenerator);

var _asyncToGenerator2 = require("babel-runtime/helpers/asyncToGenerator");

var _asyncToGenerator3 = _interopRequireDefault(_asyncToGenerator2);

var _express = require("express");

var _producer = require("../kafka/producer");

var _producer2 = _interopRequireDefault(_producer);

var _utils = require("../utils");

var _envVariables = require("../envVariables");

var _envVariables2 = _interopRequireDefault(_envVariables);

var _mdmsData = require("../utils/mdmsData");

var _mdmsData2 = _interopRequireDefault(_mdmsData);

var _create = require("../utils/create");

var _set = require("lodash/set");

var _set2 = _interopRequireDefault(_set);

var _get = require("lodash/get");

var _get2 = _interopRequireDefault(_get);

var _filter = require("lodash/filter");

var _filter2 = _interopRequireDefault(_filter);

var _logger = require("../config/logger");

var _logger2 = _interopRequireDefault(_logger);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var asyncHandler = require("express-async-handler");

exports.default = function (_ref) {
  var config = _ref.config,
      db = _ref.db;

  var api = (0, _express.Router)();
  api.post("/_createRevisedPension", asyncHandler(function () {
    var _ref2 = (0, _asyncToGenerator3.default)( /*#__PURE__*/_regenerator2.default.mark(function _callee(_ref3, res, next) {
      var body = _ref3.body;
      var payloads, processInstance, processInstances, pensionRevisionResponse, pensionRevisionList, lastPensionRevision;
      return _regenerator2.default.wrap(function _callee$(_context) {
        while (1) {
          switch (_context.prev = _context.next) {
            case 0:
              payloads = [];
              processInstance = {
                tenantId: body.ProcessInstances[0].tenantId,
                pensioner: body.ProcessInstances[0].pensioner,
                pensionRevision: body.ProcessInstances[0].pensionRevision,
                lastPensionRevision: []
              };
              processInstances = [];

              processInstances.push(processInstance);
              body.ProcessInstances = processInstances;

              _context.next = 7;
              return (0, _utils.searchPensionerForPensionRevision)(body.RequestInfo, body.ProcessInstances[0].tenantId, body.ProcessInstances[0].pensioner.pensionerNumber);

            case 7:
              pensionRevisionResponse = _context.sent;
              pensionRevisionList = pensionRevisionResponse.ProcessInstances[0].pensionRevision;

              if (pensionRevisionList.length > 0) {
                pensionRevisionList = (0, _filter2.default)(pensionRevisionList, function (x) {
                  return x.effectiveEndYear == null && x.effectiveEndMonth == null;
                });
              }

              if (!(pensionRevisionList && pensionRevisionList.length > 0)) {
                _context.next = 17;
                break;
              }

              lastPensionRevision = [];

              lastPensionRevision.push(pensionRevisionList[0]);

              body.ProcessInstances[0].lastPensionRevision = lastPensionRevision;
              _context.next = 16;
              return (0, _create.addUUIDAndAuditDetailsCloseLastRevisedPension)(body);

            case 16:
              body = _context.sent;

            case 17:
              _context.next = 19;
              return (0, _create.addUUIDAndAuditDetailsCreateRevisedPension)(body);

            case 19:
              body = _context.sent;


              payloads.push({
                topic: _envVariables2.default.KAFKA_TOPICS_CREATE_REVISED_PENSION,
                messages: JSON.stringify(body)
              });
              _producer2.default.send(payloads, function (err, data) {
                var response = {
                  ResponseInfo: (0, _utils.requestInfoToResponseInfo)(body.RequestInfo, true),
                  ProcessInstances: body.ProcessInstances
                };
                res.json(response);
              });

            case 22:
            case "end":
              return _context.stop();
          }
        }
      }, _callee, undefined);
    }));

    return function (_x, _x2, _x3) {
      return _ref2.apply(this, arguments);
    };
  }()));
  return api;
};
//# sourceMappingURL=createRevisedPension.js.map