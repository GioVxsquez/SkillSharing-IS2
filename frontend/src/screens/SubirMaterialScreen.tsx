import React, { useState, useEffect } from 'react';
import {
  View, Text, StyleSheet, TouchableOpacity, Alert,
  ActivityIndicator, StatusBar, FlatList,
} from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import { api } from '../api/config';

// US05, US27
export default function SubirMaterialScreen({ route, navigation }: any) {
  const { sesionId, sesionTitulo } = route.params;
  const [materiales, setMateriales] = useState<any[]>([]);
  const [subiendo, setSubiendo]     = useState(false);
  const [loading, setLoading]       = useState(true);

  const cargarMateriales = async () => {
    try {
      const resp = await api.get(`/materiales/sesion/${sesionId}`);
      setMateriales(resp.data.data || []);
    } catch {
      Alert.alert('Error', 'No se pudieron cargar los materiales.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarMateriales();
  }, [sesionId]);

  const handleSubirDocumento = async () => {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation'],
        copyToCacheDirectory: true,
      });

      if (result.canceled) return;

      const file = result.assets[0];
      setSubiendo(true);

      const formData = new FormData();
      formData.append('archivo', {
        uri: file.uri,
        name: file.name,
        type: file.mimeType || 'application/octet-stream',
      } as any);

      const resp = await api.post(`/materiales/sesion/${sesionId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });

      if (resp.data.ok) {
        Alert.alert('Listo', 'Material subido correctamente.');
        cargarMateriales();
      }
    } catch (error: any) {
      Alert.alert('Error', error.response?.data?.mensaje || 'No se pudo subir el archivo.');
    } finally {
      setSubiendo(false);
    }
  };

  const handleEliminar = (materialId: number, nombre: string) => {
    Alert.alert(
      'Eliminar material',
      `¿Eliminar "${nombre}"?`,
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Eliminar',
          style: 'destructive',
          onPress: async () => {
            try {
              await api.delete(`/materiales/${materialId}`);
              setMateriales((prev) => prev.filter((m) => m.materialId !== materialId));
            } catch {
              Alert.alert('Error', 'No se pudo eliminar el material.');
            }
          },
        },
      ]
    );
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#0F1C36" />

      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()}>
          <Text style={styles.backTexto}>Volver</Text>
        </TouchableOpacity>
        <View style={styles.headerTextos}>
          <Text style={styles.headerTitulo}>Materiales</Text>
          <Text style={styles.headerSub} numberOfLines={1}>{sesionTitulo}</Text>
        </View>
      </View>

      <View style={styles.infoBox}>
        <Text style={styles.infoTexto}>
          Sube archivos PDF, PPT o Word para que tus aprendices los descarguen.
        </Text>
      </View>

      {loading ? (
        <ActivityIndicator size="large" color="#F97316" style={{ marginTop: 40 }} />
      ) : (
        <FlatList
          data={materiales}
          keyExtractor={(item) => String(item.materialId)}
          contentContainerStyle={styles.lista}
          ListEmptyComponent={
            <View style={styles.vacio}>
              <Text style={styles.vacioTexto}>Aún no hay archivos subidos para esta sesión.</Text>
            </View>
          }
          renderItem={({ item }) => (
            <View style={styles.card}>
              <View style={styles.cardIcono}>
                <Text style={styles.cardIconoTexto}>{item.tipoArchivo?.slice(0, 3) || 'DOC'}</Text>
              </View>
              <View style={styles.cardDatos}>
                <Text style={styles.cardNombre} numberOfLines={1}>{item.nombre}</Text>
                <Text style={styles.cardMeta}>
                  {item.tipoArchivo} · {item.fechaSubida ? new Date(item.fechaSubida).toLocaleDateString('es-PE') : '-'}
                </Text>
              </View>
              <TouchableOpacity onPress={() => handleEliminar(item.materialId, item.nombre)} style={styles.eliminarBtn}>
                <Text style={styles.eliminarTexto}>X</Text>
              </TouchableOpacity>
            </View>
          )}
          ListFooterComponent={
            <TouchableOpacity
              style={[styles.botonSubir, subiendo && { opacity: 0.7 }]}
              onPress={handleSubirDocumento}
              disabled={subiendo}
            >
              {subiendo
                ? <ActivityIndicator color="#fff" />
                : <Text style={styles.botonSubirTexto}>+ Subir archivo</Text>}
            </TouchableOpacity>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F0F4F8' },
  header: {
    backgroundColor: '#0F1C36',
    paddingTop: 52, paddingBottom: 20, paddingHorizontal: 20,
    flexDirection: 'row', alignItems: 'flex-end', gap: 16,
  },
  backTexto: { color: '#8898AA', fontSize: 14, fontWeight: '600', marginBottom: 2 },
  headerTextos: { flex: 1 },
  headerTitulo: { color: '#FFFFFF', fontSize: 18, fontWeight: '800' },
  headerSub: { color: '#8898AA', fontSize: 12, marginTop: 2 },
  infoBox: {
    backgroundColor: '#FFF8F4', margin: 16, padding: 14,
    borderRadius: 12, borderLeftWidth: 3, borderLeftColor: '#F97316',
  },
  infoTexto: { fontSize: 13, color: '#92400E', lineHeight: 20 },
  lista: { paddingHorizontal: 16, paddingBottom: 40 },
  vacio: { alignItems: 'center', marginTop: 40 },
  vacioTexto: { fontSize: 14, color: '#8898AA', textAlign: 'center' },
  card: {
    backgroundColor: '#FFFFFF', borderRadius: 14, padding: 14, marginBottom: 10,
    flexDirection: 'row', alignItems: 'center', gap: 12,
    shadowColor: '#0F1C36', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05, shadowRadius: 6, elevation: 2,
  },
  cardIcono: {
    width: 44, height: 44, borderRadius: 12,
    backgroundColor: '#FFF0E6', justifyContent: 'center', alignItems: 'center', flexShrink: 0,
  },
  cardIconoTexto: { fontSize: 11, fontWeight: '900', color: '#F97316' },
  cardDatos: { flex: 1 },
  cardNombre: { fontSize: 14, fontWeight: '700', color: '#0F1C36' },
  cardMeta: { fontSize: 12, color: '#8898AA', marginTop: 2 },
  eliminarBtn: { padding: 8 },
  eliminarTexto: { color: '#EF4444', fontSize: 14, fontWeight: '800' },
  botonSubir: {
    marginTop: 16, backgroundColor: '#F97316', paddingVertical: 15,
    borderRadius: 14, alignItems: 'center',
    shadowColor: '#F97316', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8, elevation: 5,
  },
  botonSubirTexto: { color: '#FFFFFF', fontWeight: '800', fontSize: 15 },
});
