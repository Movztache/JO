#!/bin/bash

# ============================================================================
# SCRIPT DE HEALTH CHECK - APPLICATION VIBE-TICKETS
# ============================================================================
#
# Ce script vérifie la santé de l'application déployée et valide que tous
# les composants fonctionnent correctement.
#
# UTILISATION :
#   ./health-check.sh <URL_BASE> [TIMEOUT_SECONDS]
#
# EXEMPLES :
#   ./health-check.sh http://13.36.187.182:8080
#   ./health-check.sh http://localhost:8080 300
#
# VÉRIFICATIONS EFFECTUÉES :
# 1. Health check Spring Boot Actuator
# 2. Connectivité base de données
# 3. Endpoints API principaux
# 4. Temps de réponse
# 5. Validation des réponses JSON
#
# CODES DE RETOUR :
#   0 - Tous les checks sont passés
#   1 - Erreur de paramètres
#   2 - Application non accessible
#   3 - Base de données non accessible
#   4 - Endpoints API défaillants
#   5 - Timeout dépassé
#
# ============================================================================

set -euo pipefail

# ============================================================================
# CONFIGURATION ET VARIABLES
# ============================================================================

# Couleurs pour l'affichage
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly NC='\033[0m' # No Color

# Configuration par défaut
readonly DEFAULT_TIMEOUT=300  # 5 minutes
readonly CHECK_INTERVAL=10    # 10 secondes entre les vérifications
readonly MAX_RESPONSE_TIME=5  # 5 secondes max pour les réponses

# Variables globales
BASE_URL=""
TIMEOUT_SECONDS=""
START_TIME=""

# ============================================================================
# FONCTIONS UTILITAIRES
# ============================================================================

# Fonction d'affichage avec couleurs
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Fonction pour afficher l'aide
show_help() {
    cat << EOF
UTILISATION: $0 <URL_BASE> [TIMEOUT_SECONDS]

PARAMÈTRES:
  URL_BASE         URL de base de l'application (ex: http://localhost:8080)
  TIMEOUT_SECONDS  Timeout en secondes (défaut: ${DEFAULT_TIMEOUT})

EXEMPLES:
  $0 http://localhost:8080
  $0 http://13.36.187.182:8080 600
  $0 https://api.vibe-tickets.com 120

DESCRIPTION:
  Ce script vérifie la santé complète de l'application Vibe-Tickets
  en testant tous les composants critiques.
EOF
}

# Fonction pour valider les paramètres
validate_parameters() {
    if [[ $# -lt 1 ]]; then
        log_error "URL de base manquante"
        show_help
        exit 1
    fi

    BASE_URL="$1"
    TIMEOUT_SECONDS="${2:-$DEFAULT_TIMEOUT}"

    # Validation de l'URL
    if ! [[ "$BASE_URL" =~ ^https?://[^/]+.*$ ]]; then
        log_error "URL invalide: $BASE_URL"
        exit 1
    fi

    # Validation du timeout
    if ! [[ "$TIMEOUT_SECONDS" =~ ^[0-9]+$ ]] || [[ "$TIMEOUT_SECONDS" -lt 10 ]]; then
        log_error "Timeout invalide: $TIMEOUT_SECONDS (minimum 10 secondes)"
        exit 1
    fi

    log_info "Configuration:"
    log_info "  - URL de base: $BASE_URL"
    log_info "  - Timeout: $TIMEOUT_SECONDS secondes"
    log_info "  - Intervalle de vérification: $CHECK_INTERVAL secondes"
}

# Fonction pour vérifier si l'application répond
check_application_availability() {
    log_info "Vérification de la disponibilité de l'application..."
    
    local attempts=0
    local max_attempts=$((TIMEOUT_SECONDS / CHECK_INTERVAL))
    
    while [[ $attempts -lt $max_attempts ]]; do
        if curl -f -s --max-time $MAX_RESPONSE_TIME "$BASE_URL" > /dev/null 2>&1; then
            log_success "Application accessible"
            return 0
        fi
        
        attempts=$((attempts + 1))
        log_info "Tentative $attempts/$max_attempts - En attente..."
        sleep $CHECK_INTERVAL
    done
    
    log_error "Application non accessible après $TIMEOUT_SECONDS secondes"
    return 2
}

# Fonction pour vérifier le health check Spring Boot
check_spring_health() {
    log_info "Vérification du health check Spring Boot..."
    
    local health_url="$BASE_URL/actuator/health"
    local response
    
    if ! response=$(curl -f -s --max-time $MAX_RESPONSE_TIME "$health_url" 2>/dev/null); then
        log_error "Endpoint health non accessible: $health_url"
        return 2
    fi
    
    # Vérification que la réponse contient "UP"
    if echo "$response" | grep -q '"status":"UP"'; then
        log_success "Health check Spring Boot: OK"
        
        # Affichage des détails si disponibles
        if echo "$response" | grep -q '"db"'; then
            if echo "$response" | grep -q '"db":{"status":"UP"'; then
                log_success "Base de données: Connectée"
            else
                log_warning "Base de données: Statut incertain"
            fi
        fi
        
        return 0
    else
        log_error "Health check Spring Boot: ÉCHEC"
        log_error "Réponse: $response"
        return 3
    fi
}

# Fonction pour vérifier les endpoints API principaux
check_api_endpoints() {
    log_info "Vérification des endpoints API principaux..."
    
    local endpoints=(
        "/api/offers:GET:Offres"
        "/actuator/info:GET:Informations"
    )
    
    local failed_endpoints=0
    
    for endpoint_config in "${endpoints[@]}"; do
        IFS=':' read -r path method description <<< "$endpoint_config"
        local full_url="$BASE_URL$path"
        
        log_info "Test de l'endpoint: $description ($method $path)"
        
        if curl -f -s --max-time $MAX_RESPONSE_TIME -X "$method" "$full_url" > /dev/null 2>&1; then
            log_success "✅ $description: OK"
        else
            log_error "❌ $description: ÉCHEC"
            failed_endpoints=$((failed_endpoints + 1))
        fi
    done
    
    if [[ $failed_endpoints -eq 0 ]]; then
        log_success "Tous les endpoints API sont fonctionnels"
        return 0
    else
        log_error "$failed_endpoints endpoint(s) défaillant(s)"
        return 4
    fi
}

# Fonction pour mesurer les performances
check_performance() {
    log_info "Vérification des performances..."
    
    local health_url="$BASE_URL/actuator/health"
    local start_time
    local end_time
    local response_time
    
    start_time=$(date +%s%N)
    
    if curl -f -s --max-time $MAX_RESPONSE_TIME "$health_url" > /dev/null 2>&1; then
        end_time=$(date +%s%N)
        response_time=$(( (end_time - start_time) / 1000000 )) # Conversion en millisecondes
        
        log_info "Temps de réponse: ${response_time}ms"
        
        if [[ $response_time -lt 1000 ]]; then
            log_success "Performance: Excellente (< 1s)"
        elif [[ $response_time -lt 3000 ]]; then
            log_success "Performance: Bonne (< 3s)"
        elif [[ $response_time -lt 5000 ]]; then
            log_warning "Performance: Acceptable (< 5s)"
        else
            log_warning "Performance: Lente (> 5s)"
        fi
        
        return 0
    else
        log_error "Impossible de mesurer les performances"
        return 2
    fi
}

# Fonction pour générer un rapport final
generate_report() {
    local exit_code=$1
    local end_time=$(date +%s)
    local duration=$((end_time - START_TIME))
    
    echo ""
    log_info "============================================"
    log_info "RAPPORT DE HEALTH CHECK"
    log_info "============================================"
    log_info "URL testée: $BASE_URL"
    log_info "Durée totale: ${duration}s"
    log_info "Timestamp: $(date)"
    
    case $exit_code in
        0)
            log_success "✅ RÉSULTAT: Tous les checks sont passés"
            log_success "L'application est entièrement fonctionnelle"
            ;;
        2)
            log_error "❌ RÉSULTAT: Application non accessible"
            ;;
        3)
            log_error "❌ RÉSULTAT: Problème de base de données"
            ;;
        4)
            log_error "❌ RÉSULTAT: Endpoints API défaillants"
            ;;
        5)
            log_error "❌ RÉSULTAT: Timeout dépassé"
            ;;
        *)
            log_error "❌ RÉSULTAT: Erreur inconnue"
            ;;
    esac
    
    log_info "============================================"
}

# ============================================================================
# FONCTION PRINCIPALE
# ============================================================================
main() {
    START_TIME=$(date +%s)
    
    log_info "🏥 Démarrage du health check Vibe-Tickets"
    log_info "============================================"
    
    # Validation des paramètres
    validate_parameters "$@"
    
    # Exécution des vérifications
    local exit_code=0
    
    # 1. Vérification de la disponibilité
    if ! check_application_availability; then
        exit_code=2
    fi
    
    # 2. Health check Spring Boot (si l'app est accessible)
    if [[ $exit_code -eq 0 ]] && ! check_spring_health; then
        exit_code=3
    fi
    
    # 3. Vérification des endpoints API
    if [[ $exit_code -eq 0 ]] && ! check_api_endpoints; then
        exit_code=4
    fi
    
    # 4. Vérification des performances
    if [[ $exit_code -eq 0 ]]; then
        check_performance || true  # Ne pas faire échouer pour les performances
    fi
    
    # Génération du rapport final
    generate_report $exit_code
    
    exit $exit_code
}

# ============================================================================
# POINT D'ENTRÉE
# ============================================================================

# Vérification que le script n'est pas sourcé
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
