package com.esipeng.opengl.engine.importer;

import com.esipeng.opengl.engine.base.DrawableObjectBase;
import com.esipeng.opengl.engine.base.Mesh;
import com.esipeng.opengl.engine.base.TextureLoader;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.esipeng.opengl.engine.base.Constants.*;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.opengl.GL33.*;

public class ModelImporter extends DrawableObjectBase {
    private static final Logger logger = LoggerFactory.getLogger(ModelImporter.class);
    private List<Mesh> meshes;
    private String rootDir;
    private int numberOfIndices;
    Map<String,Integer> texturesMap = new HashMap<>();

    public ModelImporter(){
        super(1);
    }

    public ModelImporter(int instances) {
        super(instances);
    }

    public boolean loadFromResource(String resource)    {
        try {
            String path = getResourcePath(resource);
            return loadFromLocation(path);
        } catch (Exception e)   {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadFromLocation(String location) {
        AIScene aiScene = aiImportFile(location,
                aiProcess_CalcTangentSpace |
                aiProcess_JoinIdenticalVertices |
                aiProcess_Triangulate |
                aiProcess_GenNormals |
                aiProcess_GenUVCoords |
                aiProcess_TransformUVCoords |
                aiProcess_OptimizeGraph |
                aiProcess_OptimizeMeshes |
                aiProcess_FlipUVs);

        if(aiScene == null || (aiScene.mFlags() & AI_SCENE_FLAGS_INCOMPLETE) != 0 || aiScene.mRootNode() == null )  {
            logger.error("Loading {} failed.", location);
            return false;
        }

        File sceneFile = new File(location);
        rootDir = sceneFile.getParent();
        logger.info("directory of scene is {}", rootDir);

        boolean ret = processModel(aiScene);
        aiFreeScene(aiScene);
        return ret;
    }

    private boolean processModel(AIScene aiScene)   {
        if(aiScene.mNumTextures() > 0)
            logger.warn("{} embeded textures are not supported to load", aiScene.mNumTextures());

        //handle meshes
        logger.info("Loading {} meshes", aiScene.mNumMeshes());
        meshes = new LinkedList<>();
        for(int i = 0; i < aiScene.mNumMeshes(); ++i)   {
            AIMesh aiMesh = AIMesh.create(aiScene.mMeshes().get(i));
            int vbo = getManagedVBO();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            flushVertexFromAIMesh(aiMesh);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            int ebo = getManagedVBO();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            int number = flushIndicesFromAIMesh(aiMesh);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

            int aiMaterialIndex = aiMesh.mMaterialIndex();
            AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(aiMaterialIndex));
            float shininess = queryMaterialFloat(aiMaterial,AI_MATKEY_SHININESS, 0.f);

            int ambientTexture = loadOneTexture(aiMaterial, aiTextureType_AMBIENT);
            int diffuseTexture = loadOneTexture(aiMaterial, aiTextureType_DIFFUSE);
            int specularTexture =  loadOneTexture(aiMaterial, aiTextureType_SPECULAR);
            int normalTexture = loadOneTexture(aiMaterial, aiTextureType_NORMALS);
            if(normalTexture == 0)  {
                normalTexture = loadOneTexture(aiMaterial, aiTextureType_HEIGHT);
            }

            //build VAO
            int vao = getManagedVAO();
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
//        public static final int VERTEX_VEC3_POS = 4;
//        public static final int VERTEX_VEC2_TEXCOORD = 5;
//        public static final int VERTEX_VEC3_NORMAL = 6;
//        public static final int VERTEX_VEC3_TANGENT = 7;
//        public static final int VERTEX_VEC3_BITANGENT = 8;
            glVertexAttribPointer(VERTEX_VEC3_POS,
                    3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 0);
            glVertexAttribPointer(VERTEX_VEC2_TEXCOORD,
                    2,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 3);
            glVertexAttribPointer(VERTEX_VEC3_NORMAL,
                    3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 5);
            glVertexAttribPointer(VERTEX_VEC3_TANGENT,
                    3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 8);
            glVertexAttribPointer(VERTEX_VEC3_BITANGENT,
                    3,GL_FLOAT,false,Float.BYTES * 14, Float.BYTES * 11);
            glEnableVertexAttribArray(VERTEX_VEC3_POS);
            glEnableVertexAttribArray(VERTEX_VEC2_TEXCOORD);
            glEnableVertexAttribArray(VERTEX_VEC3_NORMAL);
            glEnableVertexAttribArray(VERTEX_VEC3_TANGENT);
            glEnableVertexAttribArray(VERTEX_VEC3_BITANGENT);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBindVertexArray(0);

            Mesh mesh = new Mesh(vao,ambientTexture,diffuseTexture,specularTexture,normalTexture,
                    number, shininess, true);

            meshes.add(mesh);
        }
        return true;
    }

    private void flushVertexFromAIMesh(AIMesh aiMesh)   {
//        public static final int VERTEX_VEC3_POS = 4;
//        public static final int VERTEX_VEC2_TEXCOORD = 5;
//        public static final int VERTEX_VEC3_NORMAL = 6;
//        public static final int VERTEX_VEC3_TANGENT = 7;
//        public static final int VERTEX_VEC3_BITANGENT = 8;
        FloatBuffer verticesBuf = MemoryUtil.memAllocFloat(aiMesh.mNumVertices() * 14);
        logger.debug("Loading {} vertices", aiMesh.mNumVertices());
        for(int i = 0; i < aiMesh.mNumVertices(); ++i)  {
            //pos x y z
            AIVector3D pos = aiMesh.mVertices().get(i);
            verticesBuf.put(pos.x());
            verticesBuf.put(pos.y());
            verticesBuf.put(pos.z());

            //tex coord u v
            if(aiMesh.mTextureCoords(0) == null)    {
                verticesBuf.put(0.0f);
                verticesBuf.put(0.0f);
                logger.debug("No texture found. put 0.0");
            } else  {
                AIVector3D texcoord = aiMesh.mTextureCoords(0).get(i);
                if(texcoord != null)    {
                    verticesBuf.put(texcoord.x());
                    verticesBuf.put(texcoord.y());
                } else  {
                    verticesBuf.put(0.0f);
                    verticesBuf.put(0.0f);
                    logger.debug("No texture found. put 0.0");
                }
            }

            //normal x y z
            AIVector3D normal = aiMesh.mNormals().get(i);
            verticesBuf.put(normal.x());
            verticesBuf.put(normal.y());
            verticesBuf.put(normal.z());

            //tangent x y z
            AIVector3D tangent = aiMesh.mTangents().get(i);
            verticesBuf.put(tangent.x());
            verticesBuf.put(tangent.y());
            verticesBuf.put(tangent.z());

            //bitangent x y z
            AIVector3D bitangent = aiMesh.mBitangents().get(i);
            verticesBuf.put(bitangent.x());
            verticesBuf.put(bitangent.y());
            verticesBuf.put(bitangent.z());
        }
        verticesBuf.flip();

        glBufferData(GL_ARRAY_BUFFER, verticesBuf, GL_STATIC_DRAW);
        MemoryUtil.memFree(verticesBuf);
    }

    private int flushIndicesFromAIMesh(AIMesh aiMesh) {
        logger.debug("Loading {} faces", aiMesh.mNumFaces());
        int numberOfIndices = aiMesh.mNumFaces() * 3;

        IntBuffer indicesBuf = MemoryUtil.memAllocInt(numberOfIndices);
        for(int i = 0; i < aiMesh.mNumFaces(); ++i) {
            AIFace aiFace = aiMesh.mFaces().get(i);
            if(aiFace.mNumIndices() != 3)
                logger.warn("Not a triangle!");
            for(int j = 0; j < aiFace.mNumIndices(); ++j)
                indicesBuf.put(aiFace.mIndices().get(j));
        }

        indicesBuf.flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuf, GL_STATIC_DRAW);

        return numberOfIndices;
    }

    private float queryMaterialFloat(AIMaterial aiMaterial, String aiMatKey, float defaultValue)    {
        float[] floatBuf = new float[1];
        int[] bufSize = new int[1];
        bufSize[0] = 1;
        int ret = aiGetMaterialFloatArray(aiMaterial,
                aiMatKey,
                aiTextureType_NONE,
                0,
                floatBuf,
                bufSize);
        if(ret != aiReturn_SUCCESS) {
            logger.warn("Getting {} aiMaterial float failed, returning default value", aiMatKey);
            return defaultValue;
        } else
            return floatBuf[0];
    }

    private int mapAiMapModeToGL(int mapMode)   {
        int ret;
        switch (mapMode)    {
            case aiTextureMapMode_Wrap:
                ret = GL_REPEAT;
                break;
            case aiTextureMapMode_Clamp:
                ret = GL_CLAMP;
                break;
            case aiTextureMapMode_Decal:
                ret = GL_CLAMP_TO_EDGE;
                break;
            case aiTextureMapMode_Mirror:
                ret = GL_MIRRORED_REPEAT;
                break;
            default:
                ret = GL_REPEAT;
                break;
        }
        return ret;
    }


    private int loadOneTexture(AIMaterial aiMaterial, int textureType)   {
        int textureCount = aiGetMaterialTextureCount(aiMaterial,textureType);
        logger.debug("Texture count {}", textureCount);
        if(textureCount == 0)
            return 0;

        AIString texturePath = AIString.create();
        int[] mapMode = new int[2];
        int ret = aiGetMaterialTexture(aiMaterial,textureType,0,texturePath,
                null,null,null,null,mapMode,null);
        if(ret != aiReturn_SUCCESS) {
            logger.warn("get texture path failed!");
            return 0;
        }

        int uMapMode , vMapMode;
        uMapMode = mapAiMapModeToGL(mapMode[0]);
        vMapMode = mapAiMapModeToGL(mapMode[1]);

        int textureVBO = getTexture(texturePath.dataString(), uMapMode, vMapMode);
        if(textureVBO == -1) {
            return 0;
        }

        return textureVBO;
    }


    public int getTexture(String texturePath, int uMapMode, int vMapMode) {
        String textureKey = String.format("%d$%d%s", uMapMode, vMapMode, texturePath);
        logger.debug("texture key is {}", textureKey);
        if(texturesMap.containsKey(textureKey))
            return texturesMap.get(textureKey);
        else    {
            logger.debug("Loading texture {}", texturePath);
            TextureLoader loader = new TextureLoader();
            String textureFullPath = rootDir + File.separator + texturePath;
            logger.debug("Texture full path is {}", textureFullPath);
            if(!loader.loadFromFilePath(textureFullPath))
                return -1;

            int textureVBO = glGenTextures();
            glBindTexture(GL_TEXTURE_2D,textureVBO);
            int format = -1;
            switch (loader.getNrChannel())  {
                case 1:
                    format = GL_RED;
                    break;
                case 3:
                    format = GL_RGB;
                    break;
                case 4:
                    format = GL_RGBA;
                    break;
            }
            if(format == -1)    {
                loader.release();
                return -1;
            }

            glTexImage2D(GL_TEXTURE_2D,0, format,loader.getX(), loader.getY(),0,format,GL_UNSIGNED_BYTE,loader.getData());
            glGenerateMipmap(GL_TEXTURE_2D);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, uMapMode);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, vMapMode);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glBindTexture(GL_TEXTURE_2D,0);
            loader.release();

            //put the texture into the repository
            texturesMap.put(textureKey, textureVBO);
            return textureVBO;
        }

    }

    @Override
    public Iterable<Mesh> getMeshes() {
        return meshes;
    }
}
