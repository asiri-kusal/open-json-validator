# open-json-validator
open-json-validator plugin

please add validationSchema.json to resource folder

Then add below line to application.property
validator.schema.name=validationSchema.json


Sample code 

@PostMapping("/valid")
	@JsonMapValidator
    public ResponseEntity dynamicValidation(@RequestBody HashMap<String, Object> jsonObject) {
        return  new ResponseEntity<>(jsonObject, HttpStatus.ACCEPTED);
    }

	@PostMapping("/valid")
	@JsonPojoValidator
	public ResponseEntity dynamicPojoValidation(@RequestBody MainPojo jsonObject) {
		return  new ResponseEntity<>(jsonObject, HttpStatus.ACCEPTED);
	}


