# open-json-validator
open-json-validator plugin

please add validationSchema.json to resource folder

Then add below line to application.property
validator.schema.name=validationSchema.json,validationSchema_02.json


Sample code 

@PostMapping("/valid")
	@JsonMapValidator(schemaName = "validationSchema.json")
    	public ResponseEntity dynamicValidation(@RequestBody HashMap<String, Object> jsonObject) {
        return  new ResponseEntity<>(jsonObject, HttpStatus.ACCEPTED);
    	}

	@PostMapping("/valid")
	@JsonMapValidator(schemaName = "validationSchema_02.json")
	public ResponseEntity dynamicPojoValidation(@RequestBody MainPojo jsonObject) {
		return  new ResponseEntity<>(jsonObject, HttpStatus.ACCEPTED);
	}
	
	
	* Note Please use @RequestBody as first parameter
	
-----------------------SpringBoot Application component scan-----------------------

@SpringBootApplication
@ComponentScan({"lk.open.validator", "<your main package example com.demo.project>"})
public class RestServiceApplication {


for controller advice

@ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OpenValidatorException.class)
    public void handleValidationExceptions(
        OpenValidatorException ex) {
        ex.getErrorWrapper().getErrorList().stream().forEach(errorMessage -> {
            System.out.println(errorMessage.getMessage());
            System.out.println(errorMessage.getJsonField());
            System.out.println(errorMessage.getValue());
            System.out.println(errorMessage.getJsonBlock());
            System.out.println(errorMessage.getArrayIndex());
        });

    }
    
    acceptable types in mandatory block
    list
    object
    eav
    field
    object-list



