Behaviour.specify(
  'SELECT.tools-autodiscover-dropdown',
  'tools-autodiscover.change-selection', 0, function (e) {

      // begin helper functions
      function getToolPropertyColumn(property, columnName) {
        return findElementsBySelector(property, 'TD.tool-property-' + columnName).first();
      }

      function getToolPropertyId(property) {
        return getToolPropertyColumn(property, 'id').innerHTML;
      }

      function setToolPropertyValue(property, newValue) {
        var valueNode = getToolPropertyColumn(property, 'value');
        var textBox = findElementsBySelector(valueNode, 'INPUT').first();
        textBox.value = newValue;
      }
      // end helper functions

      // Hook on onchange event to update property table accordingly on update
      e.onchange = function () {
          var toolPath = this.value;
          var detailsTable = $(this).next();
          // Find table to update
          while (detailsTable.tagName != 'TABLE'
              || !Element.hasClassName(detailsTable, 'tool-property-table')) {
            detailsTable = $(detailsTable).next();
          }

          var rows = findElementsBySelector(detailsTable, 'TR.tool-property-entry');
          for (var idx = 0; idx < rows.length; ++idx) {
            var propertyId = getToolPropertyId(rows[idx]);
            if (propertyId == "path") {
              setToolPropertyValue(rows[idx], toolPath)
            }
          }
      };
  }
);