import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ImageInfo(
    val fileName: String,
    val folderName: String,
    val localPath: String,
    val githubRawUrl: String
)

class HtmlGenerator(
    private val githubUsername: String = "nsiva7",
    private val githubRepo: String = "Data",
    private val githubBranch: String = "main",
    private val baseDirectory: String = "Images"
) {

    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "avif", "svg", "bmp", "tiff")

    fun scanImagesDirectory(imagesPath: String): Map<String, List<ImageInfo>> {
        println("🔍 Scanning Images directory: $imagesPath")

        val imagesDir = File(imagesPath)
        if (!imagesDir.exists() || !imagesDir.isDirectory) {
            println("❌ Images directory not found: $imagesPath")
            println("💡 Make sure you have the Images folder in your project directory")
            return emptyMap()
        }

        val imagesByFolder = mutableMapOf<String, MutableList<ImageInfo>>()

        // Scan all subdirectories
        imagesDir.listFiles()?.forEach { folder ->
            if (folder.isDirectory) {
                val folderName = folder.name
                println("📁 Scanning folder: $folderName")

                val imagesInFolder = mutableListOf<ImageInfo>()

                // Scan files in this folder (including nested subdirectories)
                scanFolderRecursively(folder, folderName, imagesInFolder)

                if (imagesInFolder.isNotEmpty()) {
                    imagesByFolder[folderName] = imagesInFolder
                    println("   📷 Found ${imagesInFolder.size} images")
                } else {
                    println("   📷 No images found")
                }
            }
        }

        return imagesByFolder
    }

    private fun scanFolderRecursively(
        folder: File,
        rootFolderName: String,
        imagesList: MutableList<ImageInfo>,
        relativePath: String = ""
    ) {
        folder.listFiles()?.forEach { file ->
            when {
                file.isFile && file.extension.lowercase() in imageExtensions -> {
                    val fileName = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
                    val githubPath = if (relativePath.isEmpty()) {
                        "$baseDirectory/$rootFolderName/${file.name}"
                    } else {
                        "$baseDirectory/$rootFolderName/$relativePath/${file.name}"
                    }

                    val githubRawUrl = "https://raw.githubusercontent.com/$githubUsername/$githubRepo/$githubBranch/$githubPath"

                    val imageInfo = ImageInfo(
                        fileName = fileName,
                        folderName = rootFolderName,
                        localPath = file.absolutePath,
                        githubRawUrl = githubRawUrl
                    )

                    imagesList.add(imageInfo)
                }
                file.isDirectory -> {
                    // Recursively scan subdirectories
                    val newRelativePath = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
                    scanFolderRecursively(file, rootFolderName, imagesList, newRelativePath)
                }
            }
        }
    }

    fun generateHtml(imagesByFolder: Map<String, List<ImageInfo>>): String {
        val totalImages = imagesByFolder.values.sumOf { it.size }
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html>")
            appendLine("<head>")
            appendLine("  <title>Data Images Gallery</title>")
            appendLine("  <meta charset=\"UTF-8\">")
            appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            appendLine("  <style>")
            appendLine(generateCss())
            appendLine("  </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <header>")
            appendLine("    <h1>🖼️ Images from GitHub Repository</h1>")
            appendLine("    <div class=\"stats\">")
            appendLine("      📁 ${imagesByFolder.size} folders | 📷 $totalImages images")
            appendLine("      <br><small>Generated on $timestamp</small>")
            appendLine("    </div>")
            appendLine("  </header>")
            appendLine("  ")
            appendLine("  <main id=\"image-gallery\">")

            if (imagesByFolder.isEmpty()) {
                appendLine("    <div class=\"no-images\">")
                appendLine("      <h2>No images found</h2>")
                appendLine("      <p>Make sure you have images in your Images folder and its subdirectories.</p>")
                appendLine("    </div>")
            } else {
                imagesByFolder.forEach { (folderName, images) ->
                    appendLine("    ")
                    appendLine("    <section class=\"folder-section\">")
                    appendLine("      <h2 class=\"folder-title\">📁 $folderName (${images.size} images)</h2>")
                    appendLine("      <div class=\"image-container\">")

                    images.sortedBy { it.fileName.lowercase() }.forEach { image ->
                        appendLine("        <a href=\"${image.githubRawUrl}\" target=\"_blank\" class=\"image-link\" title=\"${image.fileName}\">")
                        appendLine("          <img src=\"${image.githubRawUrl}\" alt=\"${image.fileName}\" loading=\"lazy\">")
                        appendLine("          <div class=\"image-info\">${image.fileName}</div>")
                        appendLine("        </a>")
                    }

                    appendLine("      </div>")
                    appendLine("    </section>")
                }
            }

            appendLine("  </main>")
            appendLine("  ")
            appendLine("  <footer>")
            appendLine("    <p>Generated with ❤️ by Kotlin HTML Generator</p>")
            appendLine("    <p><small>Repository: <a href=\"https://github.com/$githubUsername/$githubRepo\" target=\"_blank\">$githubUsername/$githubRepo</a></small></p>")
            appendLine("  </footer>")
            appendLine("  ")
            appendLine("  <script>")
            appendLine(generateJavaScript())
            appendLine("  </script>")
            appendLine("</body>")
            appendLine("</html>")
        }
    }

    private fun generateCss(): String = """
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }
    
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      line-height: 1.6;
      color: #333;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      min-height: 100vh;
    }
    
    header {
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(10px);
      padding: 2rem;
      text-align: center;
      margin-bottom: 2rem;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    }
    
    h1 {
      color: #2c3e50;
      margin-bottom: 1rem;
      font-size: 2.5rem;
      font-weight: 700;
    }
    
    .stats {
      color: #666;
      font-size: 1.1rem;
    }
    
    main {
      max-width: 1400px;
      margin: 0 auto;
      padding: 0 2rem 4rem;
    }
    
    .folder-section {
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(10px);
      border-radius: 20px;
      padding: 2rem;
      margin-bottom: 3rem;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
      transition: transform 0.3s ease;
    }
    
    .folder-section:hover {
      transform: translateY(-5px);
    }
    
    .folder-title {
      color: #2c3e50;
      margin-bottom: 1.5rem;
      font-size: 1.8rem;
      border-bottom: 3px solid #3498db;
      padding-bottom: 0.5rem;
    }
    
    .image-container {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 1.5rem;
    }
    
    .image-link {
      display: block;
      position: relative;
      border-radius: 15px;
      overflow: hidden;
      transition: all 0.3s ease;
      text-decoration: none;
      background: #f8f9fa;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    }
    
    .image-link:hover {
      transform: translateY(-10px) scale(1.02);
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
    }
    
    .image-link img {
      width: 100%;
      height: 200px;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    
    .image-link:hover img {
      transform: scale(1.1);
    }
    
    .image-info {
      padding: 1rem;
      color: #2c3e50;
      font-size: 0.9rem;
      font-weight: 500;
      text-align: center;
      background: linear-gradient(135deg, #f8f9fa, #e9ecef);
    }
    
    .no-images {
      text-align: center;
      padding: 4rem;
      background: rgba(255, 255, 255, 0.95);
      border-radius: 20px;
      color: #666;
    }
    
    footer {
      text-align: center;
      padding: 2rem;
      color: rgba(255, 255, 255, 0.8);
    }
    
    footer a {
      color: rgba(255, 255, 255, 0.9);
      text-decoration: none;
    }
    
    footer a:hover {
      color: #fff;
      text-decoration: underline;
    }
    
    /* Loading animation */
    .image-link img {
      opacity: 0;
      transition: opacity 0.3s ease;
    }
    
    .image-link img.loaded {
      opacity: 1;
    }
    
    /* Responsive design */
    @media (max-width: 768px) {
      .image-container {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 1rem;
      }
      
      header {
        padding: 1.5rem;
      }
      
      h1 {
        font-size: 2rem;
      }
      
      main {
        padding: 0 1rem 2rem;
      }
      
      .folder-section {
        padding: 1.5rem;
        margin-bottom: 2rem;
      }
    }
    """.trimIndent()

    private fun generateJavaScript(): String = """
    // Add loading animation to images
    document.addEventListener('DOMContentLoaded', function() {
      const images = document.querySelectorAll('.image-link img');
      
      images.forEach(img => {
        img.addEventListener('load', function() {
          this.classList.add('loaded');
        });
        
        img.addEventListener('error', function() {
          this.style.background = '#f8d7da';
          this.style.color = '#721c24';
          this.alt = '❌ Failed to load: ' + this.alt;
        });
      });
      
      // Add click analytics (optional)
      const imageLinks = document.querySelectorAll('.image-link');
      imageLinks.forEach(link => {
        link.addEventListener('click', function() {
          console.log('Image clicked:', this.title);
        });
      });
      
      // Show total load time
      window.addEventListener('load', function() {
        const loadTime = performance.now();
        console.log('Page loaded in', Math.round(loadTime), 'ms');
      });
    });
    """.trimIndent()

    fun saveHtml(html: String, outputPath: String = "index.html"): File {
        val file = File(outputPath)
        file.writeText(html)
        println("💾 HTML file saved: ${file.absolutePath}")
        return file
    }

    fun generateIndex() {
        println("🚀 GitHub Images HTML Generator")
        println("=".repeat(60))
        println()

        // Configuration - update these if needed
        val config = Config(
            githubUsername = "nsiva7",
            githubRepo = "Data",
            githubBranch = "main", // or "master"
            imagesDirectory = "Images", // Path relative to project root
            outputFile = "index.html"
        )

        println("📋 Configuration:")
        println("   GitHub: ${config.githubUsername}/${config.githubRepo}")
        println("   Branch: ${config.githubBranch}")
        println("   Images Directory: ${config.imagesDirectory}")
        println("   Output File: ${config.outputFile}")
        println()

        // Initialize the generator
        val generator = HtmlGenerator(
            githubUsername = config.githubUsername,
            githubRepo = config.githubRepo,
            githubBranch = config.githubBranch,
            baseDirectory = config.imagesDirectory
        )

        // Check if Images directory exists
        val imagesPath = File(config.imagesDirectory)
        if (!imagesPath.exists()) {
            println("❌ Images directory not found!")
            println()
            println("📁 Expected directory structure:")
            println("   YourProject/")
            println("   ├── src/")
            println("   ├── Images/           ← This should exist")
            println("   │   ├── Animals/")
            println("   │   ├── Nature/")
            println("   │   └── Technology/")
            println("   ├── build.gradle.kts")
            println("   └── index.html        ← Will be generated")
            println()
            println("💡 Solutions:")
            println("   1. Copy your Images folder to the project root")
            println("   2. Update the imagesDirectory path in main()")
            println("   3. Make sure the Images folder contains subdirectories with images")
            return
        }

        // Scan for images
        println("🔍 Scanning for images...")
        val imagesByFolder = generator.scanImagesDirectory(config.imagesDirectory)

        if (imagesByFolder.isEmpty()) {
            println("❌ No images found!")
            println()
            println("💡 Make sure your Images directory has this structure:")
            println("   Images/")
            println("   ├── FolderName1/")
            println("   │   ├── image1.jpg")
            println("   │   └── image2.png")
            println("   ├── FolderName2/")
            println("   │   └── photo1.jpeg")
            println("   └── ...")
            return
        }

        // Display scan results
        println()
        println("📊 Scan Results:")
        println("   📁 Found ${imagesByFolder.size} folders")

        imagesByFolder.forEach { (folderName, images) ->
            println("      📁 $folderName: ${images.size} images")
            images.take(3).forEach { image ->
                println("         📷 ${image.fileName}")
            }
            if (images.size > 3) {
                println("         ... and ${images.size - 3} more images")
            }
        }

        val totalImages = imagesByFolder.values.sumOf { it.size }
        println("   📷 Total: $totalImages images")
        println()

        // Generate HTML
        println("🎨 Generating HTML...")
        val html = generator.generateHtml(imagesByFolder)

        // Save to file
        val outputFile = generator.saveHtml(html, config.outputFile)

        println()
        println("✅ HTML Generation Complete!")
        println("=".repeat(60))
        println()
        println("📁 Output file: ${outputFile.absolutePath}")
        println("📊 Generated HTML with $totalImages images from ${imagesByFolder.size} folders")
        println()
        println("🚀 Next Steps:")
        println("   1. Copy ${config.outputFile} to your GitHub repository")
        println("   2. Commit and push to GitHub:")
        println("      git add ${config.outputFile}")
        println("      git commit -m \"Update image gallery\"")
        println("      git push")
        println("   3. Visit: https://${config.githubUsername}.github.io/${config.githubRepo}/")
        println("   4. Your images will load directly without API calls! 🎉")
        println()
        println("🌟 Features of generated HTML:")
        println("   ✅ No GitHub API usage (no rate limits)")
        println("   ✅ Fast loading with lazy loading")
        println("   ✅ Responsive design for mobile & desktop")
        println("   ✅ Beautiful modern styling")
        println("   ✅ Click to view full-size images")
        println("   ✅ Error handling for missing images")

        // Optional: Open the file in browser
        if (System.getProperty("os.name").lowercase().contains("mac")) {
            println()
            println("💡 Opening in browser...")
            try {
                ProcessBuilder("open", outputFile.absolutePath).start()
            } catch (e: Exception) {
                println("   Could not open automatically: ${e.message}")
            }
        }
    }
}