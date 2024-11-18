# Swagger DTO Generator Plugin

The `io.github.sereden.swagger` plugin simplifies the process of generating Kotlin 
Data Transfer Objects (DTOs) from a Swagger JSON specification. 
## Installation

Add the plugin to your `build.gradle.kts` file:

```kotlin
plugins {
    // Replace with the latest version
    id("io.github.sereden.swagger") version "1.0.0" 
}
```

## Configuration

To configure the plugin, add a `swagger` block in your `build.gradle.kts` file with the following properties:

```kotlin
swagger {
    // Path to the local Swagger JSON file
    path = File("$projectDir/swagger/api.json")
    // Package name for generated files
    packageName = "com.example.android.data.remote.dto"
    // File specifying excluded properties
    exclude = File("$projectDir/swagger/exclude.json")
    // Path to the JSON property describing DTOs
    modelPropertyPath = "components/schemas"
    // URL to download the Swagger JSON
    serverUrl = "https://petstore.swagger.io/v2/swagger.json"
}
```

### Parameters

Here’s a detailed explanation of the `swagger` block parameters:

- **`path`**
Specifies the local file path to the Swagger JSON. This file will be the source for generating DTOs.  
- **`packageName`** 
The package name for the generated DTO files
- **`exclude`** 
Path to a JSON file defining properties or paths to exclude during generation

```json
{
  "property": [
    "SomeRequest",
    "SomeResponse"
  ],
  "path": [
    "/some/path/one"
  ]
}
```
- **`modelPropertyPath`**
  JSON path to the property describing DTOs. Use `/` to navigate nested paths.
- **`serverUrl`**
  Remote URL of the Swagger JSON. You can download it locally with:
```bash
./gradlew downloadSwaggerScheme
```

### Usage
1. Apply the plugin and configure it in your Gradle file.
2. Run the generation task `./gradlew composeApp:generateCodeFromSwaggerTask` or simply `assemble` 
the app
3. Use the generated DTOs in your codebase under the specified `packageName`

### Example

The following JSON file (simplified):

<details>
  <summary>Click to reveal</summary>

```json
{
  "definitions": {
    "Pet": {
      "type": "object",
      "required": [
        "name",
        "photoUrls"
      ],
      "properties": {
        "id": {
          "type": "integer",
          "format": "int64"
        },
        "category": {
          "$ref": "#/definitions/Category"
        },
        "name": {
          "type": "string",
          "example": "doggie"
        },
        "photoUrls": {
          "type": "array",
          "xml": {
            "wrapped": true
          },
          "items": {
            "type": "string",
            "xml": {
              "name": "photoUrl"
            }
          }
        },
        "tags": {
          "type": "array",
          "xml": {
            "wrapped": true
          },
          "items": {
            "xml": {
              "name": "tag"
            },
            "$ref": "#/definitions/Tag"
          }
        },
        "status": {
          "type": "string",
          "description": "pet status in the store",
          "enum": [
            "available",
            "pending",
            "sold"
          ]
        }
      },
      "xml": {
        "name": "Pet"
      }
    }
  }
}
```
</details>

generates the following classes:

<details>
  <summary>Click to reveal</summary>

  ```kotlin
  @Serializable
  public class Pet(
    @SerialName("photoUrls")
    public val photoUrls: List<String>,
    /**
     * Example:
     * ```doggie```
     */
    @SerialName("name")
    public val name: String,
    @SerialName("id")
    public val id: Int,
    @SerialName("category")
    public val category: Category,
    @SerialName("tags")
    public val tags: List<Tag>,
    /**
     * pet status in the store
     */
    @SerialName("status")
    public val status: PetStatusOption,
  )
  
  ```
  and 
  ```kotlin
  @Serializable
  public enum class PetStatusOption(
    public val rawValue: String,
  ) {
    @SerialName("available")
    Available("available"),
    @SerialName("pending")
    Pending("pending"),
    @SerialName("sold")
    Sold("sold"),
    ;
  }
  ```
</details>
