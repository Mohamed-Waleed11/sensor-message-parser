# Kaitai Struct Hex Message Parser

This is a simple Java command-line tool that parses hexadecimal messages based on a YAML structure inspired by Kaitai Struct.

The program prompts the user to enter a HEX message and returns a JSON representation of the parsed fields according to the rules defined in `file.yaml`.

---

# Running the Application

Run the `Main` class to start the program.

The application will then prompt for HEX input in the terminal.

Example:

Enter HEX message: 00E95365000048410000C84155


The parsed result will be returned as formatted JSON.

---

# Assumptions

- If the input HEX message contains **more bytes than defined in the YAML structure**, the additional bytes are ignored.

- If the YAML structure is **invalid on application startup**, the application will fail to start.

- If the YAML file becomes **invalid during runtime**, the application will:
    - reject the new configuration
    - continue using the **previous valid configuration already loaded in memory**

- Validation checks are implemented to provide feedback about **why a YAML configuration is invalid**.

---

# Trade-offs

## Hot Reloading vs System Complexity

The application supports **hot reloading of the YAML structure** when the file is saved.

**Benefit**

- Changes to the message structure are applied immediately without restarting the application.

**Cost**

- File system watchers introduce additional complexity and can trigger multiple events during file modifications.

A more advanced solution could support real-time updates without explicitly saving the file, but that would significantly increase implementation complexity.

---

## Simplicity vs Feature Completeness

The parser currently supports a **minimal subset of Kaitai-style field definitions**.

**Benefit**

- The implementation remains simple and easy to understand.

**Cost**

- Not all possible field types or advanced parsing features such as nested structures, conditional fields, or repeating sequences are implemented.

---

# Edge Cases

**Insufficient Message Length**

If the HEX input does not contain enough bytes to populate all defined fields, parsing will fail because the required data is not available.

**Excess Message Length**

If the input HEX message contains more bytes than defined in the YAML structure, the extra bytes are ignored.

**Invalid HEX Input**

If the user provides invalid HEX characters or malformed input, parsing will fail.

**Invalid YAML Structure**

If the YAML configuration is malformed or violates validation rules, the parser will reject it and display a descriptive error message with reason of failure.