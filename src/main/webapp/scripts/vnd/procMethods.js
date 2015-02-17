/*
 * This script sets up an event handler that will update the procurementMethods property
 * based on the procurementMethodsArray multiselect values. This is needed because of
 * current Rice/KFS limitations in handling multiselect fields on maintenance documents.
 */
(function(){
	jQuery(document).ready(function() {
		var multiselectField = jQuery("select#document\\.newMaintainableObject\\.extension\\.procurementMethodsArray");
		var flattenedValuesField = jQuery("input[type=\"text\"]#document\\.newMaintainableObject\\.extension\\.procurementMethods");
		// Make sure the multiselect field is present before proceeding.
		if (multiselectField.length > 0 && flattenedValuesField.length > 0) {
			// Get the current selected values, if any.
			var currentValues = multiselectField.val();
			if (currentValues != null) {
				currentValues = currentValues.join(",");
			} else {
				currentValues = "";
			}
			
			// Update the hidden text control's value to that of the multiselect values.
			flattenedValuesField.val(currentValues);
			
			// Setup the multiselect onchange listener.
			multiselectField.change(function() {
				var procMethodsField = jQuery("input[type=\"text\"]#document\\.newMaintainableObject\\.extension\\.procurementMethods");
				if (procMethodsField.length > 0) {
					// Set the new values accordingly.
					var newValues = jQuery(this).val();
					if (newValues != null) {
						procMethodsField.val(newValues.join(","));
					} else {
						procMethodsField.val("");
					}
				}
			});
		}
	});
})();